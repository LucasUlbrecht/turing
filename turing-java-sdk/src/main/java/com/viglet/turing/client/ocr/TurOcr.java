package com.viglet.turing.client.ocr;

import com.viglet.turing.client.auth.TurServer;
import com.viglet.turing.client.utils.TurClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class TurOcr {
    private TurOcr() {
        throw new IllegalStateException("OCR Utility class");
    }

    public static String processFile(TurServer turServer, File file, boolean showOutput) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            String url = String.format("%s/api/ocr", turServer.getServerUrl());
            HttpPost httpPost = new HttpPost(url);
            HttpEntity requestEntity = MultipartEntityBuilder.create().addPart("file", fileBody).build();
            httpPost.setEntity(requestEntity);
            TurClientUtils.authentication(httpPost, turServer.getApiKey());
            String responseBody = client.execute(httpPost, response -> {
                log.info("Request Status {} - {}", response.getCode(), url);
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            });
            if (showOutput) {
                System.out.println(responseBody);
            }
            return responseBody;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}