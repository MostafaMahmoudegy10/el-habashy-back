package com.example.elhabashyback.media;

import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import com.example.elhabashyback.media.service.CloudinarySignatureService;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryUploadClientTests {

    private static final long VIDEO_BYTES = 13L * 1024L * 1024L;
    private static final String PUBLIC_ID = "test/listings/42/video";
    private static final long VERSION = 1719307544L;
    private static final String API_SECRET = "test-api-secret";

    @TempDir
    Path tempDir;

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void videoIsUploadedInSequentialSixMebibyteChunksWithOneUploadId() throws Exception {
        List<String> contentRanges = new ArrayList<>();
        List<String> uploadIds = new ArrayList<>();
        List<Long> progress = new ArrayList<>();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1_1/test-cloud/video/upload", exchange ->
                handleUpload(exchange, contentRanges, uploadIds));
        server.start();

        Path video = tempDir.resolve("video.mp4");
        try (var output = Files.newOutputStream(video)) {
            byte[] block = new byte[1024 * 1024];
            for (int index = 0; index < 13; index++) {
                output.write(block);
            }
        }

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        CloudinaryProperties properties = new CloudinaryProperties(
                "test-cloud", "test-api-key", API_SECRET, "test/listings", baseUrl);
        CloudinaryUploadClient client = new CloudinaryUploadClient(
                RestClient.create(), properties, new CloudinarySignatureService(properties));

        CloudinaryUploadResult result = client.uploadVideo(
                video,
                "video.mp4",
                "video/mp4",
                PUBLIC_ID,
                VIDEO_BYTES,
                progress::add
        );

        assertThat(contentRanges).containsExactly(
                "bytes 0-6291455/13631488",
                "bytes 6291456-12582911/13631488",
                "bytes 12582912-13631487/13631488"
        );
        assertThat(uploadIds).hasSize(3).doesNotContainNull();
        assertThat(uploadIds.stream().distinct()).hasSize(1);
        assertThat(progress).containsExactly(6291456L, 12582912L, 13631488L);
        assertThat(result.publicId()).isEqualTo(PUBLIC_ID);
        assertThat(result.bytes()).isEqualTo(VIDEO_BYTES);
        assertThat(result.duration()).isEqualTo(12.5);
    }

    @Test
    void imageIsForwardedAsOneMultipartRequestAndVerifiedBeforeItIsReturned() throws Exception {
        List<String> requestBodies = new ArrayList<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1_1/test-cloud/image/upload", exchange -> {
            requestBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            byte[] response = imageResponse().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        CloudinaryProperties properties = new CloudinaryProperties(
                "test-cloud", "test-api-key", API_SECRET, "test/listings", baseUrl);
        CloudinaryUploadClient client = new CloudinaryUploadClient(
                RestClient.create(), properties, new CloudinarySignatureService(properties));
        MockMultipartFile image = new MockMultipartFile(
                "file", "thumbnail.png", "image/png", new byte[]{1, 2, 3, 4});

        CloudinaryUploadResult result = client.uploadImage(image, PUBLIC_ID, "image/png");

        assertThat(requestBodies).singleElement().satisfies(body -> {
            assertThat(body).contains("name=\"file\"", "filename=\"thumbnail.png\"");
            assertThat(body).contains("name=\"api_key\"", "test-api-key");
            assertThat(body).contains("name=\"public_id\"", PUBLIC_ID);
        });
        assertThat(result.resourceType()).isEqualTo("image");
        assertThat(result.bytes()).isEqualTo(4);
        assertThat(result.secureUrl()).contains("thumbnail.png");
    }

    private void handleUpload(
            HttpExchange exchange,
            List<String> contentRanges,
            List<String> uploadIds
    ) throws IOException {
        String contentRange = exchange.getRequestHeaders().getFirst("Content-Range");
        contentRanges.add(contentRange);
        uploadIds.add(exchange.getRequestHeaders().getFirst("X-Unique-Upload-Id"));
        exchange.getRequestBody().transferTo(OutputStream.nullOutputStream());

        boolean done = contentRange.endsWith("13631487/13631488");
        String body = done ? finalResponse() : "{\"done\":false}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String finalResponse() {
        String signature = sha1("public_id=" + PUBLIC_ID + "&version=" + VERSION + API_SECRET);
        return """
                {
                  "secure_url": "https://res.cloudinary.com/test-cloud/video/upload/video.mp4",
                  "public_id": "%s",
                  "resource_type": "video",
                  "format": "mp4",
                  "width": 1920,
                  "height": 1080,
                  "bytes": %d,
                  "duration": 12.5,
                  "version": %d,
                  "signature": "%s",
                  "done": true
                }
                """.formatted(PUBLIC_ID, VIDEO_BYTES, VERSION, signature);
    }

    private String imageResponse() {
        String signature = sha1("public_id=" + PUBLIC_ID + "&version=" + VERSION + API_SECRET);
        return """
                {
                  "secure_url": "https://res.cloudinary.com/test-cloud/image/upload/thumbnail.png",
                  "public_id": "%s",
                  "resource_type": "image",
                  "format": "png",
                  "width": 800,
                  "height": 600,
                  "bytes": 4,
                  "version": %d,
                  "signature": "%s"
                }
                """.formatted(PUBLIC_ID, VERSION, signature);
    }

    private String sha1(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
