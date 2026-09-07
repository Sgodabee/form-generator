package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.service.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service("s3FileStoreService")
public class S3FileStoreService implements FileStoreService {

    private static final Logger log = LoggerFactory.getLogger(S3FileStoreService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileStoreService(S3Client s3Client,
                              @Value("${app.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String save(String fileName, byte[] fileContent) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileContent));

            String s3Uri = "s3://" + bucketName + "/" + fileName;
            log.info("Uploaded file to S3: {}", s3Uri);
            return s3Uri;

        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new RuntimeException("Could not upload file to S3: " + fileName, e);
        }
    }

    @Override
    public List<String> listFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> key.endsWith(".pdf"))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("Failed to list S3 objects", e);
            return Collections.emptyList();
        }
    }
}
