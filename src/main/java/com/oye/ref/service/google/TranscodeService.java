package com.oye.ref.service.google;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.security.FunctionException;
import com.oye.ref.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Configuration
public class TranscodeService {

    private static final String TRANSCODE_URL = "https://transcoder.googleapis.com/v1beta1/projects/{PROJECT_ID}/locations/us-central1/jobs";
    private static final String CHECK_URL = "https://transcoder.googleapis.com/v1beta1/{jobName}";
    private static final String GS_PATH_FORMAT = "gs://{bucketName}/{uri}";

    @Value("${google.cloud.project.id}")
    private String google_cloud_project_id;

    @Value("${google.cloud.storage.bucket}")
    private String google_cloud_storage_bucket;

    @Resource
    private CredentialService credentialService;

    @Resource
    private HttpUtil httpUtil;


    public String transcoderVideo(String templateId, String inputTarget, String outputTarget) {
        String transcodeUrl = TRANSCODE_URL.replace("{PROJECT_ID}", google_cloud_project_id);
        String baseUri = GS_PATH_FORMAT.replace("{bucketName}", google_cloud_storage_bucket);
        String transcodedVideoName = null;
        String inputUri = baseUri.replace("{uri}", inputTarget);
        String outputUri = baseUri.replace("{uri}", outputTarget);
        Map<String, String> headers = new HashMap<>();

        String authToken = "Bearer {token}";
        String accessToken = authToken.replace("{token}"
                , credentialService.getAccessToken());
//        String accessToken = authToken.replace("{token}"
//                , "ya29.c.KqQB5Qf2f174Juo4n2PVttBV3OC2RHNqLaDeu1k1dhsd0MvQXOZvk7R9cpkgpjZnndD6S8EFkQaNdq8bN9N7T5uYbSxxRJF1ZxRQdh5zUC31oGdkndvPbY1YmIeWnNZ-iUJaiE1gR0RwSd2ur84LPhcNqfm8-u7_GqWary547G3M6Ec1ASbV0UWGFAHC95rnuUcdkFoglxohjxk4mWhQkt-fjWjiBLo");

        headers.put("Authorization", accessToken);

        String jobId = doTranscode(templateId, transcodeUrl, inputUri, outputUri, headers);
        transcodedVideoName = checkTrancodeResult(templateId, transcodedVideoName, inputUri, headers, jobId);
        return transcodedVideoName;
    }

    private String doTranscode(String templateId, String transcodeUrl, String inputUri, String outputUri, Map<String, String> headers) {
        JSONObject json = new JSONObject();
        json.put("inputUri", inputUri);
        json.put("outputUri", outputUri);
        json.put("templateId", templateId);

        JSONObject createResult = httpUtil.doJsonPost(transcodeUrl, json, headers);
        String jobId;
        if (StringUtils.isNotEmpty(createResult.getString("name"))) {
            jobId = createResult.getString("name");
        } else {
            log.error("transcoder failed:\n{}", createResult.toJSONString());
            throw new FunctionException("transcoder failed:\n" + createResult.toJSONString());
        }
        return jobId;
    }

    private String checkTrancodeResult(String templateId, String transcodedVideoName, String inputUri, Map<String, String> headers, String jobId) {
        try {
            String checkUrl = CHECK_URL.replace("{jobName}", jobId);
            boolean isContinue = true;
            JSONObject jobResult;
            while (isContinue) {
                Thread.sleep(10000);
                jobResult = httpUtil.doGet(checkUrl, headers);
                log.info("templateId:{},transcode state:{},input:{},", templateId, jobResult.getString("state"), inputUri);
                if ("FAILED".equals(jobResult.getString("state"))) {
                    isContinue = false;
                    throw new FunctionException("transcode failed:" + jobResult.getString("failureReason"));
                }
                if ("SUCCEEDED".equals(jobResult.getString("state"))) {
                    isContinue = false;
                    if (templateId.contains("high-rate")) {
                        transcodedVideoName = "high-rate-mp4.mp4";
                    } else {
                        transcodedVideoName = "low-rate-mp4.mp4";
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new FunctionException(e.getMessage());
        }
        return transcodedVideoName;
    }
}
