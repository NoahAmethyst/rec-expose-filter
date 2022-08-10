package com.oye.ref.test;


import com.oye.ref.beam.PCollectionEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.state.*;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

import java.util.*;


@Slf4j
public class BeamPipe {
    private static PipelineOptions options;
    private static Pipeline pipeline;

    private static void initPipe() {
        //创建一个管道Pipeline
        options = PipelineOptionsFactory.create();
        //设置Runtime类型，当我们不指定的时候，会默认使用DirectRunner这种类型
//        options.setRunner(DataflowRunner.class);

        pipeline = Pipeline.create(options);
    }

    @Test
    public static void pipeTest() {
        List<String> LINES = Arrays.asList(
                "1,init",
                "1,do action1",
                "1,do action2",
                "1,do action3",
                "2,init",
                "1,do action4",
                "2,do action1",
                "1,end",
                "2,do action2",
                "2,do action3",
                "2,do action4",
                "2,end");

        initPipe();

        //使用原始数据创建数据集
        //PCollection表示Beam中任何大小的输入和输出数据。pipeLine读取数据输入，生成PCollection作为输出
        //使用Beam提供的Create从内存中的Java集合创建PCollection，Create接受Java Collection和一个Coder对象作为参数，在Coder指定的Collection中的元素如何编码。

        PCollection<String> words = pipeline.apply(Create.of(LINES)).setCoder(StringUtf8Coder.of());

        //统计每一个字符串的长度。
        //apply方法转化管道中的数据，转换采用PCollection（或多个PCollection）作为输入，在该集合中的每个元素上执行指定的操作，并生成新的输出PCollection
        //转换格式：[Output PCollection] = [Input PCollection].apply([Transform])
//        words.apply(
//                "ComputeWordLengths", ParDo.of(new PCollectionEntity.ComputeWordLengthFn()))
//                .apply(ParDo.of(new PCollectionEntity.MapParse()))
//                .apply(new PCollectionEntity.MapParse2())
//                .apply(RedisIO.write()
//                        .withEndpoint("redis-master.sit.blackfi.sh", 6379)
//                );


        words.apply(ParDo.of(new PCollectionEntity.StringToMap()))
                .apply(ParDo.of(new DoFn<KV<String, String>, KV<String, List<String>>>() {
                    // A state cell holding a single Integer per key+window
                    @StateId("index")
                    private final StateSpec<BagState<KV<String, String>>> indexSpec =
                            StateSpecs.bag(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()));

                    @ProcessElement
                    public void processElement(
                            ProcessContext context,
                            @StateId("index") BagState<KV<String, String>> index) {

                        String key = context.element().getKey();
                        String value = context.element().getValue();
                        if (!("init".equals(value) || "end".equals(value))) {
                            index.add(KV.of(key, value));
                        }

                        if ("end".equals(value)) {
                            Iterator<KV<String, String>> iterator = index.read().iterator();
                            List<String> valueList = new ArrayList<>();
                            while (iterator.hasNext()) {
                                KV<String, String> item = iterator.next();
                                if (key.equals(item.getKey())) {
                                    valueList.add(item.getValue());
                                    iterator.remove();
                                }
                            }
                            context.output(KV.of(key, valueList));
                        }
                    }
                }))
                .apply(ParDo.of(new DoFn<KV<String, List<String>>, String>() {
                    @ProcessElement
                    public void processElement(
                            ProcessContext context) {
                        String key = context.element().getKey();
                        List<String> valueList = context.element().getValue();
                        valueList.forEach(value -> {
                            context.output("order:" + value);
                            context.output("reverse:" + value);
                        });
                    }
                }))
                .apply(ParDo.of(new PCollectionEntity.PrintStr()));
        pipeline.run();
    }

    @Test
    public void applyTest() {
        // Create the pipeline.
        initPipe();

        PCollection<String> lines = pipeline.apply(TextIO.read().from("pom.xml"));

        lines.apply(new PCollectionEntity.CountWords()) //返回一个Map<String, Long>
                .apply(MapElements.via(new PCollectionEntity.FormatAsTextFn())) //返回一个PCollection<String>

                .apply(Window.into(FixedWindows.of(Duration.standardSeconds(10))))
                .apply("WriteCounts", TextIO.write().to("test.txt"));

        pipeline.run().waitUntilFinish();
    }

    @Test
    public void combineTest() {
        List<Integer> intP = new ArrayList<Integer>() {
            {
                for (int i = 0; i < 10; i++) {
                    add(i);
                }
            }
        };
        initPipe();
        PCollection<Integer> intPCollection = pipeline.apply(Create.of(intP));
        PCollection<Integer> pSum = intPCollection
                .apply(Combine.globally(new PCollectionEntity.IntSumFn()));
        pipeline.run().waitUntilFinish();

    }


    public static void groupTest() {

        List<String> strLists = new ArrayList<>();
        strLists.add("cat");
        strLists.add("cat");
        strLists.add("cat");
        strLists.add("dog");
        strLists.add("dog");
        strLists.add("panda");

        initPipe();
        // 创建字符串输入
        PCollection<String> words = pipeline.apply(Create.of(strLists));
        // 全部转成KV形式
        PCollection<KV<String, Integer>> kvpCollection
                = words.apply(MapElements.via(new SimpleFunction<String, KV<String, Integer>>() {
            @Override
            public KV<String, Integer> apply(String str) {
                return KV.of(str, 1);
            }
        }));
        //进行分组
        PCollection<KV<String, Iterable<Integer>>> groupKv = kvpCollection.apply(GroupByKey.create());
        groupKv.apply(MapElements.via(new PCollectionEntity.FormatAsString()));
        pipeline.run().waitUntilFinish();

    }

    @Test
    public void breakAndMerge() {
        initPipe();
        PCollection<Integer> numbers = pipeline.apply(Create.of(1, 2, 3, 4, 5));
        PCollectionList<Integer> numbersList
                = numbers.apply(Partition.of(2, new Partition.PartitionFn<Integer>() {
            // numPartitions是上面这行定义的这个2，即分区数量
            public int partitionFor(Integer input, int numPartitions) {
                // 如果是偶数，返回0，表示输出到第0个数据集中
                if (input % 2 == 0) {
                    return 0;
                }
                // 返回1表示输出到第1个数据集中
                else {
                    return 1;
                }
            }
        }));
        //指定数据集
        PCollection<Integer> pEven = numbersList.get(0);
        PCollection<Integer> pOdd = numbersList.get(1);
//        pEven.apply(ParDo.of(new PCollectionEntity.PrintInt()));
        //合并数据集
        PCollectionList<Integer> numberList = PCollectionList.of(pOdd).and(pEven);
        PCollection<Integer> mergeNumber = numberList.apply(Flatten.<Integer>pCollections());

//        mergeNumber.apply(ParDo.of(new PCollectionEntity.PrintInt()));


        pipeline.run();
    }


    public static void windowTest() {


        List<KV<Integer, String>> kvList = new ArrayList<>();
        kvList.add(KV.of(999, "0.999s"));
        kvList.add(KV.of(1000, "1.0s"));
        kvList.add(KV.of(1001, "1.001s"));
        kvList.add(KV.of(1999, "1.999s"));
        kvList.add(KV.of(2000, "2.0s"));
        kvList.add(KV.of(2500, "2.5s"));
        kvList.add(KV.of(3000, "3.0s"));
        kvList.add(KV.of(4000, "4.0s"));
        kvList.add(KV.of(5000, "5.0s"));

        initPipe();

        long NOW_TIME = Calendar.getInstance().getTime().getTime();

        PCollection<String> pTimeStr = pipeline.apply(Create.of(kvList))

                // 给每个输入元素设置Instant为(某个固定时间 + key)
                .apply(WithTimestamps.of(new SimpleFunction<KV<Integer, String>, Instant>() {
                    @Override
                    public Instant apply(KV<Integer, String> input) {
                        return new Instant(NOW_TIME + input.getKey());
                    }
                }))
                // 把Key-value转成value
                .apply(Values.create());

        PCollection<String> pTimeStrByWindow = pTimeStr.apply(Window.into(FixedWindows.of(Duration.standardSeconds(1))));
        // 同1个窗口内的合成1个列表,最多聚合100个

        pTimeStrByWindow.apply(ParDo.of(new PCollectionEntity.PrintStr()));
        pipeline.run().waitUntilFinish();

    }

    @Test
    public void pubsubTest() {

        String messageBody = "{\"created_time\":\"2020-09-28T02:23:04.027Z\",\"app_channel\":\"1\",\"user_id\":\"241978\",\"ab_test\":\"\",\"biz_params\":\"{\\\"spm\\\":\\\"01010000000\\\",\\\"video_url\\\":\\\"http://beamTest.mp4\\\",\\\"process_time\\\":\\\"0\\\",\\\"state\\\":\\\"started\\\",\\\"anchor_id\\\":\\\"293319\\\"}\",\"event_uuid\":\"2d58ed0d-7fb7-48f0-b4d9-0831b7d97484\",\"android_id\":\"1b37053eed2bacd2\",\"mac\":\"\",\"ip_city\":\"越南|0|河内|0|越南军用通讯\",\"app_name\":\"com.oye.flipchat\",\"app_version\":\"1.8.1\",\"carrier\":\"Viettel\",\"ip\":\"171.255.116.189\",\"country\":\"VN\",\"advertising_id\":\"1e014b43-f712-4651-92db-aaa337a07816\",\"spm\":\"01010000000\",\"install_time\":\"2020-09-27 05:12:15.000\",\"received_time\":\"2020-09-28T02:23:09.322Z\",\"biz_action\":\"VideoPlayState\",\"city\":\"Khu 8\",\"af_media_source\":\"organic\",\"wifi\":false}\n";
        initPipe();
        pipeline.apply(Create.of(messageBody)).setCoder(StringUtf8Coder.of())
                .apply(ParDo.of(new PCollectionEntity.MsgBodyToBizParams()))
                .apply(Window.into(SlidingWindows.of(Duration.standardDays(3)).every(Duration.standardDays(3))))
                .apply(new PCollectionEntity.BizParamsMap())
                .apply(RedisIO.write()
                        .withConnectionConfiguration(RedisConnectionConfiguration
                                .create("redis-master.sit.blackfi.sh", 6379))

                );

        pipeline.run();
    }


    public static void main(String[] args) {
//        breakAndMerge();
//        windowTest();
//        groupTest();
//        pipeTest();

        log.info("{}", System.currentTimeMillis());

    }


}

