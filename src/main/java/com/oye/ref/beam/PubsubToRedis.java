package com.oye.ref.beam;

import com.oye.ref.beam.dispose.PCollectionFunction;
import com.oye.ref.beam.dispose.PCollectionTransform;
import com.oye.ref.beam.dispose.PipePrint;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

public class PubsubToRedis {

    private static Pipeline pipeline;

    public static void main(String[] args) {
        PubSubToRedisOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToRedisOptions.class);
        options.setRunner(DataflowRunner.class);

        runPipe(options);
    }


    private static void initPipe(PubSubToRedisOptions options) {

        options.setStreaming(true);
        //set Topic
        options.setInputTopic("projects/oye-chat/topics/events");
        //set Subscription
        options.setInputSubscription("projects/oye-chat/subscriptions/event_viewer");

        pipeline = Pipeline.create(options);
    }


    private static void runPipe(PubSubToRedisOptions options) {
        initPipe(options);

        // init redis writer
        RedisIO.Write redisWriter = RedisIO.write()
                .withEndpoint(options.getRedisHost(), 6379);
        // init redis reader
        RedisIO.Read redisReader = RedisIO.read()
                .withEndpoint(options.getRedisHost(), 6379);

        PCollection<KV<String, String>> kvPipe= pipeline
                // 1) Read string messages from a Pub/Sub topic/subscription.
                .apply("Read PubSub Messages", PubsubIO.readStrings().fromSubscription(options.getInputSubscription()))
                // 2) Group the messages into sliding-sized day intervals.
                .apply(Window.into(SlidingWindows.of(Duration.standardMinutes(30))
                        .every(Duration.standardMinutes(30))))
        // convert messageBody to model object
                .apply(ParDo.of(new PCollectionFunction.MsgBodyParseBizParams()))
        // get needed value from model
                .apply(new PCollectionTransform.BizParamsToMap());
        // print value
//        .apply(ParDo.of(new PipePrint.PrintKV()));

        // save value to redis
        kvPipe.apply(redisWriter.withMethod(RedisIO.Write.Method.RPUSH));

        //get key
        PCollection<String> keyPipe = kvPipe.apply(new PCollectionTransform.KVToKeyStr());

        //print redis value
        keyPipe.apply(ParDo.of(new PipePrint.PrintStr()));

        // Execute the pipeline and wait until it finishes running.
        pipeline.run().waitUntilFinish();
    }

}
