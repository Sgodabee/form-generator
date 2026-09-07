package com.ir.formgenerator.service;

import java.util.List;


public interface FileStoreService {


    String save(String fileName, byte[] fileContent);


    List<String> listFiles();
}
