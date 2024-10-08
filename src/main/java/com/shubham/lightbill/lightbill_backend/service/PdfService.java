package com.shubham.lightbill.lightbill_backend.service;

import com.cloudinary.Cloudinary;
import com.lowagie.text.DocumentException;
import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.Consumption;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.BillRepository;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class PdfService {
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private SpringTemplateEngine templateEngine;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ThreadPoolService threadPoolService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BillService billService;

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    public String prepareInvoiceHtmlFile(User user){
        log.info("prepare html function");
        List<Bill> bills = billService.getBillsOfPastSixMonths(user);
        log.info(bills.toString() + " " + bills.size());

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat formatter = new SimpleDateFormat("dd:MM:yyyy");

        Double unitConsumption = bills.get(0).getUnitConsumption();
        log.info(unitConsumption.toString());
        String customerName = user.getName();
        log.info(customerName);
        String customerAddress = user.getAddress();
        log.info(customerAddress);
        String meterNumber = user.getMeterNumber();
        log.info(meterNumber);
        String dueDate = formatter.format(bills.get(0).getDueDate());
        log.info(dueDate);
        double amountBeforeDueDate = bills.get(0).getAmount() - bills.get(0).getDiscount();
        log.info(((Double)amountBeforeDueDate).toString());
        double amountAfterDueDate = bills.get(0).getAmount();
        log.info(((Double)amountAfterDueDate).toString());
        double totalAmountDue = 00.00;
        String currMonth = bills.get(0).getMonthOfTheBill();
        log.info(currMonth);
        log.info("In-between html");

        // Sample consumption data
        List<Consumption> consumptionSummary = new ArrayList<>();

        for (Bill bill : bills){
            consumptionSummary.add(new Consumption(
                    bill.getMonthOfTheBill(),
                    bill.getUnitConsumption(),
                    bill.getAmount(),
                    bill.getPaymentStatus() == PaymentStatus.PAID
            ));
            if(bill.getPaymentStatus() != PaymentStatus.PAID) totalAmountDue += bill.getAmount();
        }

        // Create Thymeleaf Context
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("customerAddress", customerAddress);
        context.setVariable("meterNumber", meterNumber);
        context.setVariable("dueDate", dueDate);
        context.setVariable("amountBeforeDueDate", amountBeforeDueDate);
        context.setVariable("amountAfterDueDate", amountAfterDueDate);
        context.setVariable("totalAmountDue", totalAmountDue);
        context.setVariable("consumptionSummary", consumptionSummary);
        context.setVariable("currentMonthConsumption", new Consumption(currMonth, unitConsumption, amountAfterDueDate, bills.get(0).getPaymentStatus() == PaymentStatus.PAID));

        log.info("prepare html function completed");
        return templateEngine.process("invoice-template.html", context);
    }

    public ByteArrayOutputStream generateInvoicePdf(User user) throws IOException {
        String htmlContent = prepareInvoiceHtmlFile(user);
        log.info("Html file Ready");

        // Parse the HTML to a Jsoup Document
        Document document = Jsoup.parse(htmlContent, "UTF-8");
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // Create PDF using ITextRenderer
        ITextRenderer renderer = new ITextRenderer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try{
            // Set the document
            renderer.setDocumentFromString(document.outerHtml());
            renderer.layout(); // Layout the document

            // Create the PDF
            renderer.createPDF(outputStream);
            outputStream.flush(); // Ensure all data is written

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream;
    }

    public String generateAndUploadInvoice() throws IOException {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUserId(userId);

        // Step 1: Generate the PDF and write it to a temporary file
        ByteArrayOutputStream pdfOutputStream = generateInvoicePdf(user);
        File tempPdfFile = null;
        UUID uid = UUID.randomUUID();

        try {
            // Create a temporary file to store the PDF
            tempPdfFile = File.createTempFile("invoice", ".pdf");

            // Write the PDF byte array to the temporary file
            try (FileOutputStream fos = new FileOutputStream(tempPdfFile)) {
                fos.write(pdfOutputStream.toByteArray());
            }

            // Step 2: Upload the file to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempPdfFile, Map.of(
                    "resource_type", "raw",  // Specify 'raw' for non-image files like PDF
                    "public_id", "invoices/" + uid + "_invoice", // Custom public ID
                    "overwrite", true // Overwrite if it already exists
            ));

            // Return the URL of the uploaded PDF from Cloudinary
            String prefix = "https://res-console.cloudinary.com/dwymu1d8q/media_explorer_thumbnails/";
            String suffix = "/download";
            String asset_id = (String) uploadResult.get("asset_id");

            return prefix + asset_id + suffix;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload PDF to Cloudinary", e);
        } finally {
            // Step 3: Clean up temporary file after uploading
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete();
            }
        }
    }

    public void prepareEmailAndSend(String email, File attachmentFile, String monthAndYear, String meterNumber, User user) throws MessagingException {
        // Prepare email details
        String subject = "Reminder: Electricity Bill for [Month, Year]";
        String customerName = "Shubham";
        String month = "September";
        String year = "2024";
        String attachmentFileName = "Invoice";

        String body = "Dear " + customerName + ",\n\n"
                + "I hope this message finds you well.\n\n"
                + "Please find attached your electricity bill for the month of " + monthAndYear + " for meter " + meterNumber + " . We kindly request you to review the bill, which provides a detailed breakdown of your electricity usage and charges during the specified period.\n\n"
                + "If you have any questions or concerns regarding the bill, feel free to contact our support team, and we’ll be happy to assist you.\n\n"
                + "We appreciate your prompt attention to this matter. Kindly make the payment by the due date to avoid any service interruptions.\n\n"
                + "Thank you for choosing our service.\n\n"
                + "Best regards,\n"
                + "shubham Jagdale\n"
                + "Finzly Bill Corporation\n"
                + "+91 9397197132";

        // Send the email with the attachment
        emailService.sendMailWithInvoiceAttachment(subject, email, attachmentFile, body, attachmentFileName);
    }

    public void sendInvoiceOnMail(String userId) throws IOException, MessagingException {
        User user = userRepository.findByUserId(userId);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
        String email = user.getEmail();

        Runnable task = () -> {
            File tempPdfFile = null;
            try {
                tempPdfFile = File.createTempFile("invoice", ".pdf");
                try (FileOutputStream fos = new FileOutputStream(tempPdfFile);
                     ByteArrayOutputStream pdfOutputStream = generateInvoicePdf(user)) {
                    fos.write(pdfOutputStream.toByteArray());
                }

                log.info("Ready to send mail");
                prepareEmailAndSend(email, tempPdfFile, formatter.format(new Date()), user.getMeterNumber(), user);
                log.info("mail send");
            } catch (IOException | MessagingException e) {
                logger.error("Error sending invoice on mail: " + e.getMessage(), e);
            } finally {
                if (tempPdfFile != null && tempPdfFile.exists()) {
                    boolean deleted = tempPdfFile.delete();
                    if (!deleted) {
                        logger.warn("Failed to delete temporary file: " + tempPdfFile.getAbsolutePath());
                    }
                }
            }
        };

        threadPoolService.submitTask(task);
    }
}
