package com.ir.formgenerator.service;

import com.ir.formgenerator.model.FormRecord;
import java.util.List;



public interface CsvReaderService {


    List<FormRecord> readCsv(String csvFilePath);
}
