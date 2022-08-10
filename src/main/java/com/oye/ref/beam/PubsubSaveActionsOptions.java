package com.oye.ref.beam;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation;

public interface PubsubSaveActionsOptions extends PubsubOptions, DataflowPipelineOptions {
//    @Description("The Bigtable project ID, this can be different than your Dataflow project")
//    @Default.String("bigtable-project")
//    String getBigtableProjectId();
//
//    void setBigtableProjectId(String bigtableProjectId);
//
//    @Description("The Bigtable instance ID")
//    @Default.String("bigtable-instance")
//    String getBigtableInstanceId();
//
//    void setBigtableInstanceId(String bigtableInstanceId);
//
//    @Description("The Bigtable table ID in the instance.")
//    @Default.String("bigtable-table")
//    String getBigtableTableId();
//
//    void setBigtableTableId(String bigtableTableId);

    @Description("The Cloud Pub/Sub topic to read from.")
    @Default.String("projects/oye-chat/topics/events")
    @Validation.Required
    String getInputTopic();

    @Description("Output file's window size in number of minutes.")
    @Default.Integer(1)
    Integer getWindowSize();

    void setWindowSize(Integer value);

    @Description("Path of the output file including its filename prefix.")
    String getOutput();

    void setOutput(String value);

    void setInputTopic(String s);

    void setInputSubscription(String s);

    String getInputSubscription();

    @Description("The redis host.")
    @Validation.Required
    String getRedisHost();

    void setRedisHost(String host);
}
