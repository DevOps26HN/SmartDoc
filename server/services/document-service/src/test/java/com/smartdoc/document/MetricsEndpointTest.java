package com.smartdoc.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies Task 8.1 — the Prometheus scrape endpoint is exposed and reports
 * application-level HTTP request metrics (count, latency, error dimensions) for
 * the core API, and that exercising an endpoint changes what is exported.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetricsEndpointTest {

    @Value("${local.server.port}")
    private int port;

    @Test
    void prometheusEndpointReportsHttpServerRequestMetrics() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String base = "http://localhost:" + port;

        // Exercise a core endpoint so a request metric series is recorded.
        client.send(HttpRequest.newBuilder(URI.create(base + "/api/v1/documents")).build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> scrape = client.send(
                HttpRequest.newBuilder(URI.create(base + "/actuator/prometheus")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, scrape.statusCode(), "Prometheus scrape endpoint must be available");
        String body = scrape.body();
        assertNotNull(body);

        // http.server.requests timer => request count + latency (seconds family),
        // tagged per endpoint URI and with an outcome/status dimension (error rate).
        assertTrue(body.contains("http_server_requests_seconds_count"),
                "request count metric missing");
        assertTrue(body.contains("http_server_requests_seconds_sum"),
                "request latency metric missing");
        assertTrue(body.contains("uri=\"/api/v1/documents\""),
                "core endpoint not reflected in metrics — exercising the app did not change exports");
        assertTrue(body.contains("outcome="),
                "outcome/error dimension missing");
    }
}
