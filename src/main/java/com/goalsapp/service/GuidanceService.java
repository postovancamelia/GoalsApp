package com.goalsapp.service;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GuidanceService {

    private final GoalService goalService;
    private final String guidanceSystemPrompt; // from XML
    private final RestClient restClient = RestClient.create();

    @Value("${ai.openai.base-url:}")
    private String aiBaseUrl;

    @Value("${ai.openai.key:}")
    private String aiApiKey;

    @Value("${ai.openai.model:gpt-4.1-mini}")
    private String model;

    public GuidanceService(GoalService goalService,
                           @Qualifier("guidanceSystemPrompt") String guidanceSystemPrompt) {
        this.goalService = goalService;
        this.guidanceSystemPrompt = guidanceSystemPrompt;
    }

    public String getGuidance(User user, Category category) {

        System.out.println("OpenAI base-url loaded: " + aiBaseUrl);
        System.out.println("OpenAI key loaded: " + (aiApiKey != null && !aiApiKey.isBlank()));

        List<GoalItem> items = goalService.list(user, category);

        String list = items.isEmpty()
                ? "(no items yet)"
                : items.stream().map(i -> "- " + i.getText()).collect(Collectors.joining("\n"));

        String prompt = buildPrompt(category, list);

        if (aiApiKey.isBlank() || aiBaseUrl.isBlank()) {
            return """
                   [STUB GUIDANCE]
                   System: %s

                   Prompt:
                   %s

                   (To enable real AI: set ai.openai.base-url and ai.openai.key)
                   """.formatted(guidanceSystemPrompt, prompt);
        }

        // OpenAI Responses API format
        Map<String, Object> body = Map.of(
                "model", model,
                "input", guidanceSystemPrompt + "\n\n" + prompt
        );

        try {
            Map<?, ?> resp = restClient.post()
                    .uri(aiBaseUrl)
                    .header("Authorization", "Bearer " + aiApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return extractText(resp);

        } catch (HttpClientErrorException.TooManyRequests e) {
            return """
            💗 I can’t generate guidance right now (OpenAI says: insufficient quota).

            What to do:
            1) Go to OpenAI billing and add a payment method / credits
            2) Or use a different account/project that has budget
            3) Then try again

            Technical:
            %s
            """.formatted(e.getResponseBodyAsString());

        } catch (HttpClientErrorException.Unauthorized e) {
            return """
            🥺 Authentication failed (401 Unauthorized).

            Check:
            - OPENAI_API_KEY is correct
            - No extra spaces
            - Restart IntelliJ after setting env var

            Technical:
            %s
            """.formatted(e.getResponseBodyAsString());

        } catch (HttpClientErrorException e) {
            return """
            ⚠️ OpenAI request failed (%s)

            Technical:
            %s
            """.formatted(e.getStatusCode(), e.getResponseBodyAsString());

        } catch (RestClientException e) {
            return """
            ⚠️ Network / client error while calling OpenAI.

            Technical:
            %s
            """.formatted(e.getMessage());
        }

    }

    private String extractText(Map<?, ?> resp) {
        if (resp == null) return "No response from OpenAI.";

        try {
            Object outputObj = resp.get("output");
            if (outputObj instanceof List<?> outputList && !outputList.isEmpty()) {
                Object first = outputList.get(0);
                if (first instanceof Map<?, ?> firstMap) {
                    Object contentObj = firstMap.get("content");
                    if (contentObj instanceof List<?> contentList && !contentList.isEmpty()) {
                        Object c0 = contentList.get(0);
                        if (c0 instanceof Map<?, ?> c0map) {
                            Object text = c0map.get("text");
                            if (text instanceof String s && !s.isBlank()) return s;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return "Could not parse OpenAI response.";
    }

    private String buildPrompt(Category category, String itemsList) {
        return switch (category) {
            case SHORT_TERM -> """
                    These are my short-term goals:
                    %s

                    Please give detailed advice on how I can smoothly achieve them.
                    Provide:
                    1) Step-by-step plan
                    2) Weekly schedule
                    3) Risks + mitigations
                    """.formatted(itemsList);

            case LONG_TERM -> """
                    These are my long-term goals:
                    %s

                    Help me break them into milestones with realistic timelines.
                    Provide milestones + 90-day action plan.
                    """.formatted(itemsList);

            case TODO -> """
                    These are my TODO items:
                    %s

                    Help me prioritize and propose a 7-day plan.
                    """.formatted(itemsList);

            case WISH -> """
                    These are items on my wish list:
                    %s

                    Help me turn realistic ones into goals, with budgeting/time planning.
                    """.formatted(itemsList);
        };
    }
}
