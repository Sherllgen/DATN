package com.project.evgo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Binds zalopay.* properties from application.yaml.
 * Values are injected from .env via ${ZALOPAY_*} placeholders.
 */
@Component
@ConfigurationProperties(prefix = "zalopay")
public class ZaloPayConfig {

    private String appId;
    private String key1;
    private String key2;
    private String endpoint;
    private String callbackUrl;

    public ZaloPayConfig() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Bean
    public WebClient zaloPayWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
