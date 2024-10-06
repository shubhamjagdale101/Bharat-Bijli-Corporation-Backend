package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.dto.BillDto;
import com.shubham.lightbill.lightbill_backend.dto.SignUpDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import com.shubham.lightbill.lightbill_backend.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ExcelService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private BillService billService;

    private Boolean checkForDtoConversion(String[] fields, Map<String, Integer> fieldMappingWithIndex){
        fieldMappingWithIndex.forEach((k, v) -> log.info(k + " : " + v));

        for (String field : fields) {
            if (!fieldMappingWithIndex.containsKey(field)) {
                log.info(field);
                return false;
            }
        }
        return true;
    }

    private Boolean checkForSignUpDtoConversion(Map<String, Integer> fieldMappingWithIndex){
        String[] fields = {"Name", "Email", "Address", "Phone Number"};
        return checkForDtoConversion(fields, fieldMappingWithIndex);
    }

    private Boolean checkForBillDtoConversion(Map<String, Integer> fieldMappingWithIndex){
        String[] fields = {"User Id", "MYear And Month", "Unit Consumption", "Due Date"};
        return checkForDtoConversion(fields, fieldMappingWithIndex);
    }

    private SignUpDto convertToSignUpDto(Row row, Map<String, Integer> fieldMappingWithIndex) throws Exception {
        if(!checkForSignUpDtoConversion(fieldMappingWithIndex)){
            throw new Exception("fields name are improper");
        }

        String name, email, phoneNo, address, phNo;
        name = row.getCell(fieldMappingWithIndex.get("Name")).getStringCellValue();
        email = row.getCell(fieldMappingWithIndex.get("Email")).getStringCellValue();
        address = row.getCell(fieldMappingWithIndex.get("Address")).getStringCellValue();
        phNo = row.getCell(fieldMappingWithIndex.get("Phone Number")).getStringCellValue();
        Role role = Role.CUSTOMER;


        int phNoInd = fieldMappingWithIndex.get("Phone Number");
        if(row.getCell(phNoInd).getCellType() == CellType.STRING) phoneNo = row.getCell(phNoInd).getStringCellValue();
        else phoneNo = (String) String.valueOf(row.getCell(phNoInd).getNumericCellValue());

        return SignUpDto.builder()
                .address(address)
                .name(name)
                .email(email)
                .phNo(phNo)
                .build();
    }

    private BillDto convertToBillDto(Row row, Map<String, Integer> fieldMappingWithIndex) throws Exception {
        if(checkForBillDtoConversion(fieldMappingWithIndex)){
            throw new Exception("fields name are improper");
        }

        String userId = row.getCell(fieldMappingWithIndex.get("User Id")).getStringCellValue();
        String yearAndMonth = row.getCell(fieldMappingWithIndex.get("Year And Month")).getStringCellValue();
        Integer unitConsumption = (int) row.getCell(fieldMappingWithIndex.get("Unit Consumption")).getNumericCellValue();
        Date dueDate = row.getCell(fieldMappingWithIndex.get("Due Date")).getDateCellValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(yearAndMonth, formatter);
        Date date = java.sql.Date.valueOf(localDate);

        return BillDto.builder()
                .userId(userId)
                .monthOfTheBill(date)
                .unitConsumption(unitConsumption)
                .dueDate(dueDate)
                .build();
    }

    private void rowHeadersIntoIndexing(Map<String, Integer> fieldMappingWithIndex, Row row){
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            fieldMappingWithIndex.put(cell.getStringCellValue(), i);
        }
    }

    public List<Object> saveExcelToDatabase(MultipartFile file, String className, Role role){
        List<Object> finalResult = new ArrayList<>();
        try(
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
        ){
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> fieldMappingWithIndex = new HashMap<>();

            for (Row currRow : sheet) {
                if (currRow.getRowNum() == 0) {
                    rowHeadersIntoIndexing(fieldMappingWithIndex, currRow);
                    continue;
                }

                try{
                    if(className.equals(User.class.getName())){
                        SignUpDto signUp = convertToSignUpDto(currRow, fieldMappingWithIndex);
                        User user = authService.signUpUser(signUp, role);
                        finalResult.add(user);
                    }
                    else if(className.equals(Bill.class.getName())){
                        BillDto billDto = convertToBillDto(currRow, fieldMappingWithIndex);
                        Bill bill = billService.addBill(billDto);
                        finalResult.add(bill);
                    }
                } catch(Exception ignored){
                    log.info(ignored.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return finalResult;
    }

    public List<Object> addBulkBill(MultipartFile file, Role role){
        return saveExcelToDatabase(file, Bill.class.getName(), role);
    }
}
