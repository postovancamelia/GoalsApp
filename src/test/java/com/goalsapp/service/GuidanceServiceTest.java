package com.goalsapp.service;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuidanceServiceTest {

    @Mock
    GoalService goalService;

    @Test
    void getGuidance_returnsStub_whenOpenAiConfigMissing() {
        GuidanceService service = new GuidanceService(goalService, "SYSTEM PROMPT");

        // In Spring these @Value fields default to "" via ${...:}
        ReflectionTestUtils.setField(service, "aiBaseUrl", "");
        ReflectionTestUtils.setField(service, "aiApiKey", "");

        User user = new User("alice", "hash");
        when(goalService.list(user, Category.TODO)).thenReturn(List.of(
                new GoalItem(user, Category.TODO, "task 1"),
                new GoalItem(user, Category.TODO, "task 2")
        ));

        String guidance = service.getGuidance(user, Category.TODO);

        assertThat(guidance).contains("[STUB GUIDANCE]");
        assertThat(guidance).contains("SYSTEM PROMPT");
        assertThat(guidance).contains("These are my TODO items");
        assertThat(guidance).contains("- task 1");
        assertThat(guidance).contains("- task 2");

        verify(goalService).list(user, Category.TODO);
        verifyNoMoreInteractions(goalService);
    }

    @Test
    void extractText_parsesResponsesApiShape() throws Exception {
        GuidanceService service = new GuidanceService(goalService, "SYSTEM");

        // Build a minimal "Responses API" payload that matches extractText()
        Map<String, Object> resp = Map.of(
                "output", List.of(
                        Map.of(
                                "content", List.of(
                                        Map.of("text", "Hello from OpenAI")
                                )
                        )
                )
        );

        String text = (String) ReflectionTestUtils.invokeMethod(service, "extractText", resp);

        assertThat(text).isEqualTo("Hello from OpenAI");
    }

    @Test
    void extractText_returnsFallback_whenShapeUnexpected() {
        GuidanceService service = new GuidanceService(goalService, "SYSTEM");

        String text = (String) ReflectionTestUtils.invokeMethod(service, "extractText", Map.of("x", 1));

        assertThat(text).isEqualTo("Could not parse OpenAI response.");
    }
}