package com.oye.ref.beam.google;

import com.google.cloud.bigquery.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Sample to insert rows without row ids in a table
@Slf4j
public class TableInsertRowsWithoutRowIds {

    public static void runTableInsertRowsWithoutRowIds(String project, String datasetName, String tableName, List<Map<String, Object>> rows) {

        // Create rows to insert
        List<InsertAllRequest.RowToInsert> rowContent = new ArrayList<>();
        // insertId is null if not specified
        rows.forEach(row -> {
            rowContent.add(InsertAllRequest.RowToInsert.of(row));
        });
        tableInsertRowsWithoutRowIds(project, datasetName, tableName, rowContent);
    }

    public static void tableInsertRowsWithoutRowIds(
            String project, String datasetName, String tableName, Iterable<InsertAllRequest.RowToInsert> rows) {
        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

            // Get table
            TableId tableId = TableId.of(project, datasetName, tableName);
            // Inserts rowContent into datasetName:tableId.
            InsertAllResponse response =
                    bigquery.insertAll(InsertAllRequest.newBuilder(tableId).setRows(rows).build());

            if (response.hasErrors()) {
                // If any of the insertions failed, this lets you inspect the errors
                for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                    log.error("Response error:{}", entry.getValue());
                }
            }
        } catch (BigQueryException e) {
            log.error("Insert operation not performed:{}", e.getMessage());
        }
    }


}