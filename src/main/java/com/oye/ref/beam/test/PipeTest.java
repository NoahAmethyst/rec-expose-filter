package com.oye.ref.beam.test;

import com.oye.ref.beam.dispose.PCollectionFunction;
import com.oye.ref.beam.dispose.PCollectionTransform;
import com.oye.ref.beam.dispose.PipePrint;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;

public class PipeTest {


    private static PipelineOptions options;

    private static Pipeline pipeline;

    private static void initPipe() {
        //创建一个管道Pipeline
        options = PipelineOptionsFactory.create();
        //设置Runtime类型，当我们不指定的时候，会默认使用DirectRunner这种类型
//        options.setRunner(DataflowRunner.class);

        pipeline = Pipeline.create(options);
    }


    public static void pubsubTest() {
        //VideoPlayState
        //userId:241978
        String messageBody = "{\"created_time\":\"2020-09-28T02:23:04.027Z\",\"app_channel\":\"1\",\"user_id\":\"241978\",\"ab_test\":\"\",\"biz_params\":\"{\\\"spm\\\":\\\"01010000000\\\",\\\"video_url\\\":\\\"http://redisSaveTest.mp4\\\",\\\"process_time\\\":\\\"0\\\",\\\"state\\\":\\\"started\\\",\\\"anchor_id\\\":\\\"293319\\\"}\",\"event_uuid\":\"2d58ed0d-7fb7-48f0-b4d9-0831b7d97484\",\"android_id\":\"1b37053eed2bacd2\",\"mac\":\"\",\"ip_city\":\"越南|0|河内|0|越南军用通讯\",\"app_name\":\"com.oye.flipchat\",\"app_version\":\"1.8.1\",\"carrier\":\"Viettel\",\"ip\":\"171.255.116.189\",\"country\":\"VN\",\"advertising_id\":\"1e014b43-f712-4651-92db-aaa337a07816\",\"spm\":\"01010000000\",\"install_time\":\"2020-09-27 05:12:15.000\",\"received_time\":\"2020-09-28T02:23:09.322Z\",\"biz_action\":\"VideoPlayState\",\"city\":\"Khu 8\",\"af_media_source\":\"organic\",\"wifi\":false}\n";
        initPipe();
        pipeline.apply(Create.of(messageBody)).setCoder(StringUtf8Coder.of())
                .apply(ParDo.of(new PCollectionFunction.MsgBodyParseBizParams()))
                .apply(new PCollectionTransform.BizParamsToMap())
                .apply(ParDo.of(new PipePrint.PrintKV()))
//                .apply(ParDo.of(new PCollectionFunction.SaveRedis()));
                .apply(RedisIO.write().withEndpoint("redis-master.sit.blackfi.sh", 6379)
                .withMethod(RedisIO.Write.Method.RPUSH));
        pipeline.run();
    }

    public static void main(String[] args) {
        pubsubTest();
    }
}
