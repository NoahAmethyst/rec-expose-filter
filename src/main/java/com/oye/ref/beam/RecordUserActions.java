package com.oye.ref.beam;

import com.oye.ref.beam.dispose.FeaturesFunction;
import com.oye.ref.beam.dispose.PCollectionFunction;
import com.oye.ref.beam.dispose.PCollectionTransform;
import com.oye.ref.beam.model.UserAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;


@Slf4j
public class RecordUserActions {

    private static Pipeline pipeline;

    public static final String recordPayActionsStateId = "pay-record-actions";
    public static final String funnelPayActionsStateId = "funnel-record-actions";
    public static final String recordPayAttributions = "recordPayAttributions";
    public static final String recordFunnelActions = "recordFunnelActions";
    public static final String recordReadedMessage = "recordReadedMessage";

    public static final String SavePayFunnel = "save_pay_funnel";

    public static final String Table_Pay_Funnel = "pay_funnel";

    public static final String robot_topic="oye-robot-topic";


    public static void main(String[] args) {
        PubsubSaveActionsOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(PubsubSaveActionsOptions.class);
        options.setRunner(DataflowRunner.class);
        options.setWorkerMachineType("n2-standard-2");
        runPipe(options);
    }


    private static void initPipe(PubsubSaveActionsOptions options) {

        options.setStreaming(true);
        //set Topic
        options.setInputTopic("projects/oye-chat/topics/events");
        //set Subscription
        options.setInputSubscription("projects/oye-chat/subscriptions/event_viewer");

        pipeline = Pipeline.create(options);
    }

    private static void runPipe(PubsubSaveActionsOptions options) {
        initPipe(options);

        // init redis writer
        RedisIO.Write redisWriter = RedisIO.write()
                .withEndpoint(options.getRedisHost(), 6379);


        PCollection<UserAction> actions = pipeline
                // 1) Read string messages from a Pub/Sub topic/subscription.
                .apply("Read PubSub Messages", PubsubIO.readStrings().fromTopic(options.getInputTopic()))
                // 2) Group the messages into sliding-sized day intervals.
                .apply(Window.into(SlidingWindows.of(Duration.standardMinutes(30))
                        .every(Duration.standardMinutes(30))))
                // convert messageBody to userAction
                .apply("Transform Pubsub message to UserAction", new PCollectionTransform.MsgBodyToUserAction());

        //group key actions
        PCollection<KV<String, Iterable<UserAction>>> keyActionGroup =
                actions.apply(Window.into(SlidingWindows.of(Duration.standardMinutes(30))
                        .every(Duration.standardMinutes(30))))
                        .apply("Group userAction By userId", MapElements.via(new SimpleFunction<UserAction, KV<String, UserAction>>() {
                            @Override
                            public KV<String, UserAction> apply(UserAction userAction) {
                                return KV.of(userAction.getUserId(), userAction);
                            }
                        }))
                        // group user actions
                        .apply(GroupByKey.create());

        //dispose key actions
        PCollection<KV<String, String>> nodesToRedis = keyActionGroup.apply("Build action nodes", MapElements
                .via(new PCollectionFunction.DisposeActionGroup()))
                // build KV(previous action - current action : 1)
                .apply("Format action nodes as A-B ", ParDo.of(new PCollectionFunction.ActionNodesListParseKV()));

        //save nodes and occurred number to redis
        nodesToRedis.apply("Save nodes to redis", redisWriter.withMethod(RedisIO.Write.Method.INCRBY));


        //filter pay key actions
        actions.apply("Filter key pay action node", ParDo.of(new PCollectionFunction.PayUserActionFilter()))
                //mapping userId and action
                .apply("Mapping userId and action", MapElements.via(new SimpleFunction<UserAction, KV<String, UserAction>>() {
                    @Override
                    public KV<String, UserAction> apply(UserAction userAction) {
                        return KV.of(userAction.getUserId(), userAction);
                    }
                }))
                //record actions when paying happens
                .apply("Record actions when paying happens", ParDo.of(new PCollectionFunction.RecordPayAttributions(recordPayActionsStateId)))
                //filter action records
                .apply("Filter action records", ParDo.of(new PCollectionFunction.ActionTimeFilter()))
                // save action records to BigQuery
                .apply("Save action records to BigQuery", ParDo.of(new PCollectionFunction.SavePayAttributionsToBigQuery()));


        //record user acton to BigQuery
        keyActionGroup.apply("Build chat node", MapElements
                .via(new PCollectionFunction.BuildChatNodes()))
                .apply("save chat nodes to BigQuery", ParDo.of(new PCollectionFunction.SaveChatNodeToBigQuery()));

        //filter pay funnel actions
        actions.apply("Filter pay funnel actions", ParDo.of(new PCollectionFunction.PayFunnelActionFilter()))
                //mapping userId and action
                .apply("Mapping userId and action", MapElements.via(new SimpleFunction<UserAction, KV<String, UserAction>>() {
                    @Override
                    public KV<String, UserAction> apply(UserAction userAction) {
                        return KV.of(userAction.getUserId(), userAction);
                    }
                }))
                //record actions
                .apply("Record actions when start paying happens", ParDo.of(new PCollectionFunction.RecordActions(recordFunnelActions, options.getRedisHost(), 6379)))
                //filter action records
                .apply("Filter action records", ParDo.of(new PCollectionFunction.FunnelActionFilter()))
                //save to BigQuery
                .apply("Save funnel action result to BigQuery", ParDo.of(new PCollectionFunction.SaveActionsToBigQuery(SavePayFunnel, Table_Pay_Funnel)));

        actions.apply("Change message status if readed", ParDo.of(new PCollectionFunction.ChangeMessageStatus(options.getRedisHost(), 6379)))
                .apply("send pubsub message",ParDo.of(new FeaturesFunction
                        .SendPubsubMessage(robot_topic)));

        actions.apply(Window.into(SlidingWindows.of(Duration.standardMinutes(30))
                .every(Duration.standardMinutes(30))))
                .apply("Group userAction By userId every 30 minutes", MapElements.via(new SimpleFunction<UserAction, KV<String, UserAction>>() {
                    @Override
                    public KV<String, UserAction> apply(UserAction userAction) {
                        return KV.of(userAction.getUserId(), userAction);
                    }
                }))
                // create group
                .apply("create group for standby application", GroupByKey.create())
                .apply("simple dispose just for example", MapElements
                        .via(new PCollectionFunction.SimpleDispose()));


        pipeline.run().waitUntilFinish();
    }

}
