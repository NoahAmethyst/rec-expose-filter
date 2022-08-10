package com.oye.ref.service.google;


import com.alibaba.fastjson.JSONObject;
import com.google.cloud.bigquery.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Configuration
public class BigQueryService {

    @Value("${google.cloud.auth.bigquerycredential}")
    private String bigqueryCredentialPath;

    @Value("${google.cloud.project.id}")
    private String google_cloud_project_id;

    @Resource
    private BigQuery googleBigQuery;

    public List<Object> runSimpleQuery(String querySql) {
        return simpleQuery(querySql);
    }

    private List<Object> simpleQuery(String query) {
        List<Object> results = new ArrayList<>();
        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.

            // Create the query job.
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                    .setPriority(QueryJobConfiguration.Priority.BATCH)
                    .build();
            // Execute the query.
            TableResult result = googleBigQuery.query(queryConfig);
            // return the results.
            Iterator<Field> fieldIterator = result.getSchema().getFields().iterator();
            List<String> fieldNameList = new ArrayList<>();
            while (fieldIterator.hasNext()) {
                fieldNameList.add(fieldIterator.next().getName());
            }

            result.iterateAll().forEach(
                    rows -> {
                        AtomicInteger index = new AtomicInteger(0);
                        JSONObject object = new JSONObject();
                        rows.forEach(row -> {
                            object.put(fieldNameList.get(index.get()), row.getValue());
                            index.getAndIncrement();
                        });
                        results.add(object);
                    });

        } catch (BigQueryException | InterruptedException e) {
            log.error("Query did not run,cause:{}", e.toString());
        }
        return results;
    }


}
