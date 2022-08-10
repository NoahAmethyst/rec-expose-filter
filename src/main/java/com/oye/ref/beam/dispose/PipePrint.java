package com.oye.ref.beam.dispose;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

@Slf4j
public class PipePrint {


    /**
     * Print String PCollection
     */
    public static class PrintStr extends DoFn<String, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String strValue = c.element();
            // Use ProcessContext.output to emit the output element.
            log.info("print String: {}", strValue);
            c.output(strValue);
        }
    }

    /**
     * Print Integer PCollection
     */
    public static class PrintInt extends DoFn<Integer, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            Integer intValue = c.element();
            // Use ProcessContext.output to emit the output element.
            log.info("print Integer: {}", intValue);
            c.output(intValue.toString());
        }
    }

    /**
     * Print Object PCollection
     */
    public static class PrintObj extends DoFn<Object, Object> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            Object objValue = c.element();
            // Use ProcessContext.output to emit the output element.
            log.info("print Object: {}", JSONObject.toJSONString(objValue));
            c.output(objValue);
        }
    }


    /**
     * Print KV<String,String> PCollection
     */
    public static class PrintKV extends DoFn<KV<String, String>, KV<String, String>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            KV<String, String> objValue = c.element();
            // Use ProcessContext.output to emit the output element.
            String printContext = objValue.getKey() + " " + objValue.getValue();
            log.info("print Object: {}", printContext);
            c.output(objValue);
        }
    }

}
