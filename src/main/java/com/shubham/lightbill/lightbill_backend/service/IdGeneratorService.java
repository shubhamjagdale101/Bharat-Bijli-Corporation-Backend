package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.model.*;
import com.shubham.lightbill.lightbill_backend.repository.IdGeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IdGeneratorService {
    @Autowired
    private IdGeneratorRepository idGeneratorRepository;
    private static final Map<String, String> prefixValues = new HashMap<>();
    public IdGeneratorService(){
        prefixValues.put(User.class.getName(), "USR-");
        prefixValues.put(Transaction.class.getName(), "TXN-");
        prefixValues.put(Wallet.class.getName(), "WLT-");
        prefixValues.put(Bill.class.getName(), "BIL-");
        prefixValues.put("METER", "MTR-");
    }

    public String generateId(String className){
        UniqueID id = idGeneratorRepository.findByName(className);
        int value;

        if(id == null) {
            id = UniqueID.builder()
                    .name(className)
                    .counter(1)
                    .build();
            value = 0;
            idGeneratorRepository.save(id);
        }
        else {
            value = id.getCounter();
            id.setCounter(id.getCounter()+1);
            idGeneratorRepository.save(id);
        }

        String prefix = prefixValues.get(className);
        return prefix + id.getCounter();
    }
}
