package com.robiulsunyemon.profile_service.profile.service;
import com.robiulsunyemon.profile_service.profile.dto.NidResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class NidService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NidResponseDto parseNid(MultipartFile frontImage, MultipartFile backImage) throws Exception {

        String url = geminiApiUrl + "?key=" + apiKey;

        String frontBase64 = Base64.getEncoder().encodeToString(frontImage.getBytes());
        String backBase64 = Base64.getEncoder().encodeToString(backImage.getBytes());


        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Front Image Part
        parts.add(Map.of("inlineData", Map.of("mimeType", Objects.requireNonNull(frontImage.getContentType()), "data", frontBase64)));
        // Back Image Part
        parts.add(Map.of("inlineData", Map.of("mimeType", Objects.requireNonNull(backImage.getContentType()), "data", backBase64)));
        // Text Prompt Part
        parts.add(Map.of("text", "Extract the Name (English and Bangla), NID Number, Date of Birth, and Address from these Bangladeshi NID card images. Be accurate."));

        requestBody.put("contents", List.of(Map.of("parts", parts)));

        // ৩. Structured Output Configuration (JSON Schema)
        Map<String, Object> jsonSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "nameEn", Map.of("type", "STRING"),
                        "nameBn", Map.of("type", "STRING"),
                        "nidNumber", Map.of("type", "STRING"),
                        "dateOfBirth", Map.of("type", "STRING", "description", "Format: YYYY-MM-DD"),
                        "address", Map.of("type", "STRING")
                ),
                "required", List.of("nameEn", "nameBn", "nidNumber", "dateOfBirth", "address")
        );

        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", jsonSchema
        ));

        // ৪. API Call
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                var root = objectMapper.readTree(response.getBody());
                String jsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

                return objectMapper.readValue(jsonText, NidResponseDto.class);
            } else {
                throw new RuntimeException("Failed to process images: Unexpected status " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Gemini API Error: " + e.getResponseBodyAsString(), e);
        }
    }

}