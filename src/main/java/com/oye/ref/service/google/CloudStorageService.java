package com.oye.ref.service.google;

import com.google.cloud.storage.*;
import com.oye.ref.security.FunctionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Configuration
@Slf4j
public class CloudStorageService {

    @Value("${google.cloud.project.id}")
    private String google_cloud_project_id;

    @Value("${google.cloud.storage.bucket}")
    private String google_cloud_storage_bucket;

    @Value("${google.cloud.auth.credential}")
    private String credentialPath;

    @Resource
    private CredentialService credentialService;

    private Storage init() {
        return StorageOptions.newBuilder()
                .setCredentials(credentialService.initCredentials(credentialPath))
                .setProjectId(google_cloud_project_id)
                .build()
                .getService();
    }



    public void download(String sourceUri, String destFilePath) {
        Storage storage = init();
        Blob blob = storage.get(BlobId.of(google_cloud_storage_bucket, sourceUri));
        blob.downloadTo(Paths.get(destFilePath));
    }

    public void upload(String rootPath, String fileName, String filePath) {
        log.info("upload file to GC:{}", filePath);
        try {
            Storage storage = init();
            BlobId blobId = BlobId.of(google_cloud_storage_bucket, rootPath + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            log.info("upload file to GC failed:{},reason:{}", filePath, e.getMessage());
            throw new FunctionException("upload file to GC failed");
        }
    }

}
