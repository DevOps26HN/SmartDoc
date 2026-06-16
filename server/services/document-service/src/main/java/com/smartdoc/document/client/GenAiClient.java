package com.smartdoc.document.client;

import com.smartdoc.document.config.GenAiProperties;
import com.smartdoc.document.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Thin REST client the server uses to talk to the internal GenAI microservice.
 *
 * <p>This is the only place that knows the GenAI service exists. The client app
 * never calls GenAI directly — it always goes through the server, which calls
 * GenAI over this defined REST interface.</p>
 */
@Component
public class GenAiClient {

    private final RestClient restClient;
    private final GenAiProperties properties;

    public GenAiClient(RestClient genAiRestClient, GenAiProperties properties) {
        this.restClient = genAiRestClient;
        this.properties = properties;
    }

    public SummaryResponseDto summarize(String text, String title, Integer maxWords) {
        int words = maxWords != null ? maxWords : properties.getDefaultMaxWords();
        GenAiSummarizeRequest body = new GenAiSummarizeRequest(text, title, words);
        return post(properties.getSummarizePath(), body, SummaryResponseDto.class);
    }

    public AnswerResponseDto ask(String text, String question, String title) {
        GenAiAskRequest body = new GenAiAskRequest(text, question, title);
        return post(properties.getAskPath(), body, AnswerResponseDto.class);
    }

    public GenAiHealthResponse health() {
        try {
            return restClient.get()
                    .uri(properties.getHealthPath())
                    .retrieve()
                    .body(GenAiHealthResponse.class);
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GenAI service returned an error: " + ex.getResponseBodyAsString(), ex);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "GenAI service is unreachable: " + ex.getMessage(), ex);
        }
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        try {
            return restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException ex) {
            // GenAI replied with a 4xx/5xx (e.g. backend misconfigured, model error).
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GenAI service returned an error: " + ex.getResponseBodyAsString(), ex);
        } catch (ResourceAccessException ex) {
            // GenAI not reachable (network / not started).
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "GenAI service is unreachable: " + ex.getMessage(), ex);
        }
    }
}
