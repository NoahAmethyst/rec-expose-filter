package com.oye.ref.beam;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubOptions;
import org.apache.beam.sdk.options.*;

public interface PubSubToRedisOptions extends PubsubOptions {

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
