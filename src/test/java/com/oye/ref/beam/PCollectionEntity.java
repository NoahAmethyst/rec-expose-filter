package com.oye.ref.beam;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.BizParams;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.HashMap;
import java.util.Map;

public class PCollectionEntity {

    public static class CountWords extends PTransform<PCollection<String>,
            PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {

            // 将文本行转换成单个单词
            PCollection<String> words = lines.apply(
                    ParDo.of(new ExtractWordsFn()));

            // 计算每个单词次数
            PCollection<KV<String, Long>> wordCounts =
                    words.apply(Count.<String>perElement());

            return wordCounts;
        }
    }

    /**
     * 1.a.通过Dofn编程Pipeline使得代码很简洁。b.对输入的文本做单词划分，输出。
     */
    static class ExtractWordsFn extends DoFn<String, String> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            if (c.element().trim().isEmpty()) {
                return;
            }
            // 将文本行划分为单词
            String[] words = c.element().split("[^a-zA-Z']+");
            // 输出PCollection中的单词
            for (String word : words) {
                if (!word.isEmpty()) {
                    c.output(word);
                }
            }
        }
    }


    /**
     * 2.格式化输入的文本数据，将转换单词为并计数的打印字符串。
     */
    public static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String> {
        @Override
        public String apply(KV<String, Long> input) {
            System.out.println(input.getKey() + ": " + input.getValue());
            return input.getKey() + ": " + input.getValue();
        }
    }

    /**
     * 传递给ParDo的DoFn对象中包含对输入集合中的元素的进行处理,DoFn从输入的PCollection一次处理一个元素
     */
    public static class ComputeWordLengthFn extends DoFn<String, Map<String, Integer>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String word = c.element();
            // Use ProcessContext.output to emit the output element.
            System.out.println(word.length());
            c.output(new HashMap<String, Integer>() {
                {
                    put(word, word.length());
                }
            });
        }
    }

    public static class MapParse extends DoFn<Map<String, Integer>, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            Map<String, Integer> input = c.element();
            // Use ProcessContext.output to emit the output element.
            input.forEach((key, value) -> {
                System.out.println(key + ":" + value);
                c.output(key);
            });
        }
    }

    public static class MapParse2 extends PTransform<PCollection<String>,
            PCollection<KV<String, String>>> {
        @Override
        public PCollection<KV<String, String>> expand(PCollection<String> lines) {

            PCollection<KV<String, String>> result = lines.apply(ParDo.of(new StringToMap()));

            return result;
        }
    }


    public static class StringToMap extends DoFn<String, KV<String, String>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String value = c.element().split(",")[1];
            String key = c.element().split(",")[0];
            // Use ProcessContext.output to emit the output element.

            c.output(KV.of(key, value));

        }
    }

    /**
     * 继承自Combine.CombineFn<A,B,C>
     * A输入管道的元素， B中间累加器的类型，  C输出结果类型
     * 步骤：创建累加器、各机器管道元素合到累加器中、各管道累加器合并、处理最终结果
     */
    public static class IntSumFn extends Combine.CombineFn<Integer, Integer, Integer> {

        // 中间累加器可以自己定义
        // 中间累加器初始化
        @Override
        public Integer createAccumulator() {
            return 0;
        }

        //单管道中的累加器操作
        @Override
        public Integer addInput(Integer accum, Integer input) {
            accum += input;
            return accum;
        }

        //合并多个分布式机器的累加器方法
        //最终返回1个累加器
        @Override
        public Integer mergeAccumulators(java.lang.Iterable<Integer> accumulators) {
            Integer merged = createAccumulator();
            for (Integer accum : accumulators) {
                merged += accum;
            }

            return merged;
        }

        //如何将累加器转化为你需要的输出结果
        //这里可以对最后聚合的输出结果做特殊处理
        @Override
        public Integer extractOutput(Integer accum) {
            System.out.println(accum);
            return accum;
        }
    }


    public static class FormatAsString extends SimpleFunction<KV<String, Iterable<Integer>>, String> {
        @Override
        public String apply(KV<String, Iterable<Integer>> input) {
            String str = "[key]:[value]";
            String sb = new String();
            Iterable<Integer> value = input.getValue();

            while (value.iterator().hasNext()) {
                sb += value.iterator().next() + ",";
            }
            str = str.replace("[key]", input.getKey());
            str = str.replace("[value]", sb);
            return str;
        }
    }

    public static class PrintStr extends DoFn<String, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String word = c.element();
            // Use ProcessContext.output to emit the output element.
            System.out.println(word);
            c.output(word);
        }
    }

    public static class PrintInt extends DoFn<Integer, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            Integer value = c.element();
            // Use ProcessContext.output to emit the output element.
            System.out.println(value);
            c.output(value.toString());

        }
    }


    public static class PrintObj extends DoFn<Object, Object> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            Object value = c.element();
            // Use ProcessContext.output to emit the output element.
            System.out.println(JSONObject.toJSONString(value));
            c.output(value);

        }
    }

    public static class MsgBodyToBizParams extends DoFn<String, BizParams> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String json = c.element();
            // Use ProcessContext.output to emit the output element.
            JSONObject jsonObj = JSONObject.parseObject(json);

            //VideoPlayState
            if (jsonObj != null && "VideoPlayState".equals(jsonObj.getString("biz_action"))) {
                String bizJsonStr = jsonObj.getString("biz_params");
                BizParams bizParams = JSONObject.parseObject(bizJsonStr, BizParams.class);
                if ("started".equals(bizParams.getState())) {
                    bizParams.setUserId(jsonObj.getString("user_id"));
                    c.output(bizParams);
                }
            }
        }
    }

    public static class BizParamsToMap extends DoFn<BizParams, KV<String, String>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            BizParams params = c.element();
            // Use ProcessContext.output to emit the output element.
            KV<String, String> map = KV.of("testStr", params.getVideoUrl());
            c.output(map);
        }
    }

    public static class BizParamsMap extends PTransform<PCollection<BizParams>,
            PCollection<KV<String, String>>> {

        @Override
        public PCollection<KV<String, String>> expand(PCollection<BizParams> input) {
            PCollection<KV<String, String>> bizParamsMap = input.apply(ParDo.of(new BizParamsToMap()));
            return bizParamsMap;
        }
    }


}
