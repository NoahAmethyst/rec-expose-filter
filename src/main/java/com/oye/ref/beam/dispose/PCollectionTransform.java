package com.oye.ref.beam.dispose;

import com.oye.ref.beam.model.PubsubBizParams;
import com.oye.ref.beam.model.UserAction;
import com.oye.ref.beam.model.UserBizAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

@Slf4j
public class PCollectionTransform {


    /**
     * Transform PCollection from PubsubBizParams to KV<String,String>
     */
    public static class BizParamsToMap extends PTransform<PCollection<PubsubBizParams>,
            PCollection<KV<String, String>>> {
        @Override
        public PCollection<KV<String, String>> expand(PCollection<PubsubBizParams> input) {
            PCollection<KV<String, String>> bizParamsMap = input.apply(ParDo.of(new PCollectionFunction.BizParamsParseMap()));
            return bizParamsMap;
        }
    }

    public static class MsgBodyToUserAction extends PTransform<PCollection<String>,
            PCollection<UserAction>> {
        @Override
        public PCollection<UserAction> expand(PCollection<String> input) {
            PCollection<UserAction> userAction = input.apply(ParDo.of(new PCollectionFunction
                    .MsgBodyParseUserAction()));
            return userAction;
        }
    }

    public static class MsgBodyToFeatureParams extends PTransform<PCollection<String>,
            PCollection<UserBizAction>> {
        @Override
        public PCollection<UserBizAction> expand(PCollection<String> input) {
            PCollection<UserBizAction> userFeatures = input.apply(ParDo.of(new FeaturesFunction
                    .MsgBodyParseFeatureParams()));
            return userFeatures;
        }
    }

    /**
     * Transform PCollection from KV<String,String> get key to String
     */
    public static class KVToKeyStr extends PTransform<PCollection<KV<String, String>>,
            PCollection<String>> {
        @Override
        public PCollection<String> expand(PCollection<KV<String, String>> input) {
            PCollection<String> key = input.apply(ParDo.of(new PCollectionFunction.GetMapKey()));
            return key;
        }
    }
}
