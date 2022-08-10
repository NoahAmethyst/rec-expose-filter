package com.oye.ref.service.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.oye.ref.security.FunctionException;
import com.oye.ref.utils.CommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CredentialService {


    private static final String URL = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";

    @Resource
    private CommandUtil commandUtil;

    public GoogleCredentials initCredentials(String credentialPath) {
        GoogleCredentials credentials;
        ClassPathResource resource = new ClassPathResource(credentialPath);
        log.info("load credential file:{}", resource.getFilename());
        InputStream serviceAccountStream = null;
        try {
            serviceAccountStream = resource.getInputStream();
            return ServiceAccountCredentials.fromStream(serviceAccountStream);
        } catch (Exception e) {
            throw new FunctionException("initialize google credential error:" + e.getMessage());
        }
    }

    public String getAccessToken() {

        String accessToken = null;
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("gcloud");
            cmd.add("auth");
            cmd.add("print-access-token");
            accessToken = commandUtil.command(cmd);

        } catch (Exception e) {
            log.error("get google accessToken error:{}", e.getMessage());
        }
        if (StringUtils.isEmpty(accessToken)) {
            throw new FunctionException("no access token.");
        } else {
            log.info("load google cloud accessToken success");
            return accessToken;
        }
    }
}
