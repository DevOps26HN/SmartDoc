package com.smartdoc.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised configuration for reaching the GenAI microservice.
 *
 * <p>Nothing here is hardcoded in the source: the base URL, paths and timeouts
 * are all supplied via {@code application.properties} / environment variables
 * (locally {@code GENAI_BASE_URL=http://genai:8000}, on the cluster the in-cluster
 * Service name injected from a ConfigMap).</p>
 */
@ConfigurationProperties(prefix = "genai")
public class GenAiProperties {

    /** Base URL of the GenAI service, e.g. http://genai:8000 (compose) or the in-cluster Service. */
    private String baseUrl = "http://localhost:8000";

    private String summarizePath = "/summarize";
    private String askPath = "/ask";
    private String healthPath = "/health";

    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 120;

    /** Default summary length when the caller does not specify one. */
    private int defaultMaxWords = 80;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSummarizePath() {
        return summarizePath;
    }

    public void setSummarizePath(String summarizePath) {
        this.summarizePath = summarizePath;
    }

    public String getAskPath() {
        return askPath;
    }

    public void setAskPath(String askPath) {
        this.askPath = askPath;
    }

    public String getHealthPath() {
        return healthPath;
    }

    public void setHealthPath(String healthPath) {
        this.healthPath = healthPath;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public int getDefaultMaxWords() {
        return defaultMaxWords;
    }

    public void setDefaultMaxWords(int defaultMaxWords) {
        this.defaultMaxWords = defaultMaxWords;
    }
}
