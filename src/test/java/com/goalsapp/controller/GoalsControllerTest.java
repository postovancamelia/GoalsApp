package com.goalsapp.controller;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import com.goalsapp.service.GoalService;
import com.goalsapp.service.GuidanceService;
import com.goalsapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalsController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoalsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GoalService goalService;

    @MockitoBean
    GuidanceService guidanceService;

    @MockitoBean
    UserService userService;

    private static Principal principal(String name) {
        return () -> name;
    }

    @Test
    void categoryPage_rendersCategoryView_withItems() throws Exception {
        User user = new User("alice", "hash");
        when(userService.findByUsernameOrThrow("alice")).thenReturn(user);

        List<GoalItem> items = List.of(new GoalItem(user, Category.TODO, "task"));
        when(goalService.list(user, Category.TODO)).thenReturn(items);

        mockMvc.perform(get("/goals/TODO").principal(principal("alice")))
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attribute("category", Category.TODO))
                .andExpect(model().attribute("items", items));

        verify(userService).findByUsernameOrThrow("alice");
        verify(goalService).list(user, Category.TODO);
        verifyNoInteractions(guidanceService);
    }

    @Test
    void addItem_redirectsBackToCategory() throws Exception {
        User user = new User("alice", "hash");
        when(userService.findByUsernameOrThrow("alice")).thenReturn(user);

        mockMvc.perform(post("/goals/SHORT_TERM/add")
                        .principal(principal("alice"))
                        .param("text", "hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals/SHORT_TERM"));

        verify(goalService).add(user, Category.SHORT_TERM, "hello");
    }

    @Test
    void guidanceOnSamePage_rendersCategoryView_withGuidance() throws Exception {
        User user = new User("alice", "hash");
        when(userService.findByUsernameOrThrow("alice")).thenReturn(user);

        List<GoalItem> items = List.of(new GoalItem(user, Category.LONG_TERM, "big goal"));
        when(goalService.list(user, Category.LONG_TERM)).thenReturn(items);
        when(guidanceService.getGuidance(user, Category.LONG_TERM)).thenReturn("guidance text");

        mockMvc.perform(post("/goals/LONG_TERM/guidance").principal(principal("alice")))
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attribute("category", Category.LONG_TERM))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("guidance", "guidance text"));

        verify(goalService).list(user, Category.LONG_TERM);
        verify(guidanceService).getGuidance(user, Category.LONG_TERM);
    }
}