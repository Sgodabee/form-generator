package com.ir.formgenerator.service;


public interface TransferSpeedLogger {


    void logComparison(String fileName, long localMillis, long s3Millis);
}
