package com.CVRgram.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${ai.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String prompt) {

        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty()) {
            return "I am not integreted for now !!";
        }

        // Use a currently supported model
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                + apiKey;

        // Build request body
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user"); // important
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // ✅ Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("candidates")) {

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");

                if (!candidates.isEmpty()) {

                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");

                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");

                    return parts.get(0).get("text").toString();
                }
            }

            return "No response from AI.";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}