package com.ir.formgenerator.service;

import com.ir.formgenerator.model.AuditLog;
import java.util.List;


public interface AuditService {


    void recordAsync(String username, String csvFileInput,
                     String pdfFileOutput, String fileDestination);

    List<AuditLog> findAll();
}
