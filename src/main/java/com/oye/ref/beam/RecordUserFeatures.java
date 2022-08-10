package com.oye.ref.beam;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.beam.dispose.FeaturesFunction;
import com.oye.ref.beam.dispose.PCollectionTransform;
import com.oye.ref.beam.model.ReportActionModel;
import com.oye.ref.beam.model.UserBizAction;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

public class RecordUserFeatures {

    private static Pipeline pipeline;

    private static String alertTopic = "projects/oye-chat/topics/action-alert";

    private static int redisPort = 6379;

    public static void main(String[] args) {
        PubsubSaveActionsOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(PubsubSaveActionsOptions.class);
        options.setRunner(DataflowRunner.class);
        options.setWorkerMachineType("n2-standard-2");
        runPipe(options);
    }


    private static void initPipe(PubsubSaveActionsOptions options) {

        options.setStreaming(true);
        options.setInputTopic("projects/oye-chat/topics/user_business_action");
        options.setInputSubscription("projects/oye-chat/subscriptions/business_listener");

        pipeline = Pipeline.create(options);
    }


    private static void runPipe(PubsubSaveActionsOptions options) {
        initPipe(options);

        // init redis writer
        RedisIO.Write redisWriter = RedisIO.write()
                .withEndpoint(options.getRedisHost(), 6379);


        PCollection<UserBizAction> singleFeature = pipeline
                // 1) Read string messages from a Pub/Sub topic/subscription.
                .apply("Read PubSub Messages", PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
                // 2) Group the messages into sliding-sized day intervals.
                .apply(Window.into(SlidingWindows.of(Duration.standardMinutes(30))
                        .every(Duration.standardMinutes(30))))
                // convert messageBody to userAction
                .apply("Transform Pubsub message to UserFeatureParams", new PCollectionTransform.MsgBodyToFeatureParams());


        PCollection<ReportActionModel> reportSingleAction = singleFeature.apply("save business action to redis", ParDo.of(new FeaturesFunction
                .SaveActionsToRedis(options.getRedisHost(), redisPort)))
                .apply("calculate features to redis", ParDo.of(new FeaturesFunction
                        .CalculateFeature(options.getRedisHost(), redisPort)));
//                .apply("Transform business action to report model", new PCollectionTransform.MsgBodyToFeatureParams())


        reportSingleAction.apply("convert to Pub/Sub message", ParDo.of(new DoFn<ReportActionModel, String>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                c.output(JSONObject.toJSONString(c.element()));
            }
        }))
                .apply("send alert pubsub message", ParDo.of(new FeaturesFunction
                        .SendPubsubMessage(alertTopic)));


        pipeline.run().waitUntilFinish();
    }


}
