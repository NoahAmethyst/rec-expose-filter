package com.oye.ref.service.google;


import com.alibaba.fastjson.JSONObject;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.oye.ref.security.FunctionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@Configuration
public class PubsubService {

    @Value("${google.cloud.auth.pubsubcredential}")
    private String pubsubCredentialPath;

    @Value("${google.cloud.project.id}")
    private String google_cloud_project_id;

    @Resource
    private CredentialService credentialService;


    public void sendMessageToPubsubTopic(String topic, JSONObject messageBody) {
        try {
            log.info("send pubsub message to topic {},\nmessage body:{}", topic, messageBody.toJSONString());
            Publisher publisher = initPublisher(topic);
            ByteString data = ByteString.copyFromUtf8(messageBody.toString());
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .build();
            publisher.publish(pubsubMessage);
        } catch (Exception e) {
            log.error("send message to topic {}  error:{}", topic, e.getMessage());
            throw new FunctionException("send message to topic error.");
        }
    }

    private Publisher initPublisher(String pubsubTopic) {
        try {
            GoogleCredentials credentials = credentialService.initCredentials(pubsubCredentialPath);
            ProjectTopicName topicName = ProjectTopicName.of(google_cloud_project_id, pubsubTopic);
            Publisher publisher = Publisher.newBuilder(topicName)
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            return publisher;
        } catch (Exception e) {
            log.error("init topic publisher {} error:{}", pubsubTopic, e.getMessage());
            throw new FunctionException("init publisher error.");
        }
    }


}
