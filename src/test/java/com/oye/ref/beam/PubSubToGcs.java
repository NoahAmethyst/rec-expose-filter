package com.oye.ref.beam;


import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubOptions;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.ParDo;

public class PubSubToGcs {
    /*
     * Define your own configuration options. Add your own arguments to be processed
     * by the command-line parser, and specify default values for them.
     */
    public interface PubSubToGcsOptions extends PubsubOptions, PipelineOptions, StreamingOptions {
        @Description("The Cloud Pub/Sub topic to read from.")
//        @Default.String("projects/oyechat/topics/demotopic")
        @Required
        String getInputTopic();

        @Description("Output file's window size in number of minutes.")
        @Default.Integer(1)
        Integer getWindowSize();

        void setWindowSize(Integer value);

        @Description("Path of the output file including its filename prefix.")
        @Required
        String getOutput();

        void setOutput(String value);

        void setInputTopic(String s);

        void setInputSubscription(String s);

        String getInputSubscription();
    }

    public static void main(String[] args) {
        // The maximum number of shards when writing output.
        int numShards = 1;

        PubSubToGcsOptions options =
                PipelineOptionsFactory.create().as(PubSubToGcsOptions.class);
        options.setStreaming(true);
//        options.setPubsubRootUrl("http://localhost:8755");
//        options.setProject("oye-chat");
        options.setInputTopic("projects/oye-chat/topics/events");
        options.setInputSubscription("projects/oye-chat/subscriptions/event_viewer");


        Pipeline pipeline = Pipeline.create(options);

        pipeline
                // 1) Read string messages from a Pub/Sub topic.
                .apply("Read PubSub Messages", PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
//                // 2) Group the messages into fixed-sized minute intervals.
//                .apply(Window.into(FixedWindows.of(Duration.standardDays(3))))
                // 3) Write one file to GCS for every window of messages.
//                .apply("Write Files to GCS", new WriteOneFilePerWindow(options.getOutput(), numShards));
                // 4) Print string to console
                .apply(ParDo.of(new PCollectionEntity.PrintStr()));
        // Execute the pipeline and wait until it finishes running.

        pipeline.run().waitUntilFinish();
    }

}