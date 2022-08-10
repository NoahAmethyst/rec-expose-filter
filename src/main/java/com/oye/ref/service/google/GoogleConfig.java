package com.oye.ref.service.google;


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GoogleConfig {

    @Value("${google.cloud.auth.bigquerycredential}")
    private String bigqueryCredentialPath;

    @Value("${google.cloud.project.id}")
    private String google_cloud_project_id;

    @Resource
    private CredentialService credentialService;



    @Bean("googleBigQuery")
    public BigQuery init() {
        return BigQueryOptions.newBuilder()
                .setCredentials(credentialService.initCredentials(bigqueryCredentialPath))
                .setProjectId(google_cloud_project_id)
                .build()
                .getService();
    }

}
