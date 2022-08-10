package com.oye.ref.service.dispose;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.security.FunctionException;
import com.oye.ref.service.google.CloudStorageService;
import com.oye.ref.service.google.PubsubService;
import com.oye.ref.service.google.TranscodeService;
import com.oye.ref.utils.CommandUtil;
import com.oye.ref.utils.HttpUtil;
import com.oye.ref.utils.QiniuUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DisposeVideoService {

    //    private static final String ROOT_PATH = "/Users/amethyst/Downloads/file";
    private static final String ROOT_PATH = "/tmp";

    private static final String VIDEO_TYPE = "video";

    private static final String LOW_RATE_TRANSCODE_JOB_ID = "oye-transcode-video-low-rate";
    private static final String HIGH_RATE_TRANSCODE_JOB_ID = "oye-transcode-video-high-rate";

    public static final String transcodeSuccessTopic = "video-transcode-success";


    @Resource
    private HttpUtil httpUtil;

    @Resource
    private CommandUtil commandUtil;

    @Resource
    private TranscodeService transcodeService;

    @Resource
    private CloudStorageService cloudStorageService;

    @Resource
    private QiniuUtil qiniuUtil;

    @Resource
    private PubsubService pubsubService;

    @Async
    public void disposeVideo(JSONObject request) {
        // if root path is not exist then make it
        File dir = new File(ROOT_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!VIDEO_TYPE.equals(request.getString("type"))) {
            log.info("file type is not video.");
            return;
        }

        String sourceFileName = request.getString("name");
        String sourceFileUrl = request.getString("url");
        if (StringUtils.isEmpty(sourceFileName) || StringUtils.isEmpty(sourceFileUrl)) {
            log.info("sourceFileName or sourceFileUrl is empty.");
            return;
        }
        //Todo 放开校验
//        if (!MyStringUtil.md5Encode(VIDEO_TYPE + sourceFileName + sourceFileUrl)
//                .equals(request.getString("sign"))) {
//            log.info("sign is incorrect.");
//            return;
//        }

        log.info("download source file from uri:{}", sourceFileUrl);
        File sourceFile = download(sourceFileUrl, sourceFileName);

        String sourceFilePath = sourceFile.getAbsolutePath();
        String outputFilePath = ROOT_PATH + "/" + "faststart_" + sourceFileName;


        log.info("do faststart to video file:{}", sourceFileName);
        fastStartVideo(sourceFilePath, outputFilePath);
        // delete old file
        sourceFile.delete();
        // rename output file
        sourceFile = new File(outputFilePath);
        sourceFile.renameTo(new File(sourceFilePath));
        sourceFile = new File(sourceFilePath);

        // upload source file to google cloud
        try {
            uploadFileToGC(sourceFile.getName(), sourceFilePath);

            // transcode video in google cloud
            String lowRateFileName = "low_rate_" + sourceFile.getName();
            String highRateFileName = "high_rate_" + sourceFile.getName();

            //transcode low rate and download
            String lowRateVideoUri = transcoderVideo(LOW_RATE_TRANSCODE_JOB_ID, sourceFile.getName());
            String lowRateFilePath = ROOT_PATH + "/" + lowRateFileName;
            downloadFileFromGC(lowRateVideoUri, lowRateFilePath);

            //transcode high rate and download
            String highRateVideoUri = transcoderVideo(HIGH_RATE_TRANSCODE_JOB_ID, sourceFile.getName());
            String highRateFilePath = ROOT_PATH + "/" + highRateFileName;
            downloadFileFromGC(highRateVideoUri, highRateFilePath);

            // upload file to qiniu
            uploadFileToQiniu(lowRateFilePath, lowRateFileName);
            uploadFileToQiniu(highRateFilePath, highRateFileName);

            // delete temp file
            new File(lowRateFilePath).delete();
            new File(highRateFilePath).delete();
            log.info("transcode success");

            sendSuccessMessage(sourceFile.getName(), lowRateFileName, highRateFileName, lowRateVideoUri, highRateVideoUri);
        } catch (Exception e) {
            log.error("dispose video error:{}", e.getMessage());
        } finally {
            sourceFile.delete();
        }
    }

    private File download(String url, String name) {
        return httpUtil.getNetFile(url, name, ROOT_PATH);
    }

    private String fastStartVideo(String inputFile, String outputFile) {
        List<String> cmd = new ArrayList();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(inputFile);
        cmd.add("-c");
        cmd.add("copy");
        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add(outputFile);
        try {
            return commandUtil.command(cmd);
        } catch (IOException e) {
            throw new FunctionException("run cmd error:" + e.getMessage());
        }
    }


    private String transcoderVideo(String jobId, String fileName) {
        log.info("transcode file:{}", fileName);
        String inputUri = "videos/match/" + fileName;
        String outputUri = ("videos/match-trans/" + fileName).replace(".mp4", "/");
//        String inputUri = "test/" + fileName;
//        String outputUri = ("test/trans/" + fileName).replace(".mp4", "/");
        String transcodedFileMame = transcodeService.transcoderVideo(jobId, inputUri, outputUri);
        return outputUri + transcodedFileMame;
    }

    private String uploadFileToGC(String fileName, String filePath) {
        String rootPath = "videos/match/";
//        String rootPath = "test/";
        cloudStorageService.upload(rootPath, fileName, filePath);
        return rootPath + fileName;
    }

    private void downloadFileFromGC(String sourceUri, String destFilePath) {
        try {
            log.info("down load file from GC:{}", sourceUri);
            cloudStorageService.download(sourceUri, destFilePath);
        } catch (Exception e) {
            log.error("download file from google cloud storage error:", e.getMessage());
            throw new FunctionException("download file from google cloud storage error:" + e.getMessage());
        }
    }

    private void uploadFileToQiniu(String uploadFilePath, String fileName) {
        qiniuUtil.upload(uploadFilePath, fileName);
    }

    private void sendSuccessMessage(String originalVideo, String qiniuLowRateVideo, String qiniuHighRateVideo, String gcsLowRateVideo, String gcsHighRateVideo) {
        JSONObject messageBody = new JSONObject();
        messageBody.put("orginalVideo", originalVideo);
        messageBody.put("qiniuLowRateVideoUri", qiniuLowRateVideo);
        messageBody.put("qiniuHighRateVideoUri", qiniuHighRateVideo);
        messageBody.put("gcsLowRateVideoUri", gcsLowRateVideo);
        messageBody.put("gcsHighRateVideoUri", gcsHighRateVideo);
        pubsubService.sendMessageToPubsubTopic(transcodeSuccessTopic, messageBody);
    }
}
