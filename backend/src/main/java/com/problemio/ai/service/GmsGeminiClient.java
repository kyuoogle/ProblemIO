package com.problemio.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class GmsGeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GmsGeminiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gms.base-url:https://gms.ssafy.io/gmsapi/}")
    private String baseUrl;

    @Value("${gms.api-key}")
    private String apiKey;

    @Value("${gms.model:gemini-2.0-flash-exp-image-generation}")
    private String model;

    public GmsGeminiClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public byte[] generatePngBytes(String title, String description, String styleHint) {
        String prompt = buildPrompt(title, description, styleHint);
        return requestImage(prompt);
    }

    private byte[] requestImage(String prompt) {
        log.info("GMS Gemini request baseUrl={} model={}", baseUrl, model);
        String endpoint = buildEndpoint();

        // Gemini 2.0 Flash Image Generation Payload
        // https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp-image-generation:generateContent
        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseModalities", List.of("Text", "Image")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // GMS uses query param 'key' for Gemini models
        String url = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("key", apiKey)
                .toUriString();

        try {
            String requestBody = objectMapper.writeValueAsString(payload);
            log.info("Request Body: {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("GMS Error Status: {} Body: {}", response.getStatusCode(), response.getBody());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            return extractImageBytes(response.getBody());

        } catch (Exception e) {
            log.error("Failed to generate image via GMS Gemini", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] extractImageBytes(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Gemini Response Structure:
        // candidates[0].content.parts[0].inlineData.data (Base64)
        // OR candidates[0].content.parts[0].inline_data.data (Base64)

        JsonNode candidates = root.get("candidates");
        if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode parts = firstCandidate.path("content").path("parts");

            if (parts.isArray() && !parts.isEmpty()) {
                for (JsonNode part : parts) {
                    JsonNode inlineData = part.get("inlineData");
                    if (inlineData == null) {
                        inlineData = part.get("inline_data");
                    }

                    if (inlineData != null && inlineData.has("data")) {
                        String base64Data = inlineData.get("data").asText();
                        return Base64.getDecoder().decode(base64Data);
                    }
                }
            }
        }

        log.error("No image data found in response: {}", responseBody);
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String buildEndpoint() {
        // gms.base-url might already contain the full path
        // e.g.,
        // https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/

        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String targetPath = "generativelanguage.googleapis.com/v1beta/models/";

        if (base.contains(targetPath)) {
            // If base url already contains the target path, just append the model and
            // method
            return base + model + ":generateContent";
        }

        // Otherwise append the full path
        return base + targetPath + model + ":generateContent";
    }

    private String buildPrompt(String title, String description, String styleHint) {
        return String.format(
                "Create a high-quality quiz thumbnail image.\n\n" +
                        "Title: %s\n" +
                        "Description: %s\n" +
                        "Style: %s\n\n" +
                        "Key Requirements:\n" +
                        "1. Square aspect ratio (1:1).\n" +
                        "2. No text, letters, or numbers in the image.\n" +
                        "3. Modern, clean, and eye-catching design.\n" +
                        "4. Abstract or iconic representation of the quiz topic.",
                title, description, styleHint);
    }
}
