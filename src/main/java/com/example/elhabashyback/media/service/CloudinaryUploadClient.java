package com.example.elhabashyback.media.service;

import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import com.example.elhabashyback.media.exception.MediaUploadException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.function.LongConsumer;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadClient {

    public static final int VIDEO_CHUNK_SIZE = 6 * 1024 * 1024;

    private final RestClient cloudinaryRestClient;
    private final CloudinaryProperties properties;
    private final CloudinarySignatureService signatureService;

    public CloudinaryUploadResult uploadImage(
            Path stagedFile,
            String fileName,
            String publicId,
            String contentType,
            long expectedBytes
    ) {
        ensureConfigured();
        MultiValueMap<String, Object> parts = signedParts(publicId, Instant.now().getEpochSecond());
        parts.add("file", filePart(stagedFile, fileName, contentType));
        CloudinaryResponse response = send("image", parts, new HttpHeaders());
        return validateFinalResponse(response, publicId, "image", expectedBytes);
    }

    public CloudinaryUploadResult uploadVideo(
            Path stagedFile,
            String fileName,
            String contentType,
            String publicId,
            long totalBytes,
            LongConsumer progressListener
    ) {
        ensureConfigured();
        String uploadId = UUID.randomUUID().toString();
        CloudinaryResponse response = null;
        long start = 0;

        try (InputStream input = Files.newInputStream(stagedFile)) {
            byte[] buffer = new byte[VIDEO_CHUNK_SIZE];
            int count;
            while ((count = readChunk(input, buffer)) > 0) {
                long end = start + count - 1;
                byte[] chunk = count == buffer.length ? buffer : Arrays.copyOf(buffer, count);
                long timestamp = Instant.now().getEpochSecond();
                MultiValueMap<String, Object> parts = signedParts(publicId, timestamp);
                parts.add("file", bytePart(chunk, fileName, contentType));

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Unique-Upload-Id", uploadId);
                headers.set("Content-Range", "bytes " + start + "-" + end + "/" + totalBytes);
                response = send("video", parts, headers);
                start = end + 1;
                progressListener.accept(start);
            }
        } catch (IOException exception) {
            throw new MediaUploadException("Could not read the staged video", exception);
        }

        if (start != totalBytes || response == null) {
            throw new MediaUploadException("Video upload did not consume the complete file");
        }
        return validateFinalResponse(response, publicId, "video", totalBytes);
    }

    private MultiValueMap<String, Object> signedParts(String publicId, long timestamp) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("api_key", properties.apiKey());
        parts.add("timestamp", Long.toString(timestamp));
        parts.add("signature", signatureService.signUpload(publicId, timestamp));
        parts.add("public_id", publicId);
        return parts;
    }

    private HttpEntity<?> filePart(Path file, String fileName, String contentType) {
        FileSystemResource resource = new FileSystemResource(file) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new HttpEntity<>(resource, headers);
    }

    private HttpEntity<?> bytePart(byte[] bytes, String fileName, String contentType) {
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new HttpEntity<>(resource, headers);
    }

    private CloudinaryResponse send(
            String resourceType,
            MultiValueMap<String, Object> parts,
            HttpHeaders requestHeaders
    ) {
        try {
            return cloudinaryRestClient.post()
                    .uri(properties.resolvedApiBaseUrl()
                            + "/v1_1/" + properties.cloudName() + "/" + resourceType + "/upload")
                    .headers(headers -> headers.addAll(requestHeaders))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(parts)
                    .retrieve()
                    .body(CloudinaryResponse.class);
        } catch (RestClientException exception) {
            throw new MediaUploadException("Cloudinary rejected the media upload", exception);
        }
    }

    private CloudinaryUploadResult validateFinalResponse(
            CloudinaryResponse response,
            String expectedPublicId,
            String expectedResourceType,
            long expectedBytes
    ) {
        if (response == null || Boolean.FALSE.equals(response.done())) {
            throw new MediaUploadException("Cloudinary did not finish the media upload");
        }
        if (!expectedPublicId.equals(response.publicId())
                || !expectedResourceType.equalsIgnoreCase(response.resourceType())) {
            throw new MediaUploadException("Cloudinary returned media that does not match the upload");
        }
        if (response.bytes() == null || response.bytes() != expectedBytes) {
            throw new MediaUploadException("Cloudinary returned an unexpected media size");
        }
        String urlPrefix = "https://res.cloudinary.com/" + properties.cloudName() + "/";
        if (response.secureUrl() == null || !response.secureUrl().startsWith(urlPrefix)) {
            throw new MediaUploadException("Cloudinary returned an invalid secure URL");
        }
        if (response.version() == null || response.signature() == null
                || !signatureService.verifyUploadResponse(
                response.publicId(), response.version(), response.signature())) {
            throw new MediaUploadException("Cloudinary response signature is invalid");
        }
        if (response.format() == null || response.format().isBlank()) {
            throw new MediaUploadException("Cloudinary did not return the media format");
        }
        if ("video".equals(expectedResourceType) && response.duration() == null) {
            throw new MediaUploadException("Cloudinary did not return the video duration");
        }
        return new CloudinaryUploadResult(
                response.secureUrl(),
                response.publicId(),
                response.resourceType().toLowerCase(Locale.ROOT),
                response.format().toLowerCase(Locale.ROOT),
                response.width(),
                response.height(),
                response.bytes(),
                response.duration(),
                response.version(),
                response.signature()
        );
    }

    private int readChunk(InputStream input, byte[] buffer) throws IOException {
        int total = 0;
        while (total < buffer.length) {
            int read = input.read(buffer, total, buffer.length - total);
            if (read < 0) {
                break;
            }
            total += read;
        }
        return total;
    }

    public void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new MediaUploadException("Cloudinary is not configured");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CloudinaryResponse(
            @JsonProperty("secure_url") String secureUrl,
            @JsonProperty("public_id") String publicId,
            @JsonProperty("resource_type") String resourceType,
            String format,
            Integer width,
            Integer height,
            Long bytes,
            Double duration,
            Long version,
            String signature,
            Boolean done
    ) {
    }
}
