package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemGiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemGiveService itemGiveService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 물품_지급_성공() throws Exception {
        // given
        Long itemId = 1L;
        String requestBody = "{\"student_id\": 2}";

        doNothing().when(itemGiveService).giveItemToStudent(eq(itemId), eq(2L), any());

        // when & then
        mockMvc.perform(post("/items/{itemId}/give", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("물품 지급이 완료되었습니다."));
    }

    @Test
    void 물품_지급_실패_이미_지급된_물품() throws Exception {
        // given
        Long itemId = 1L;
        String requestBody = "{\"student_id\": 2}";

        doThrow(new IllegalStateException("해당 물품은 이미 지급 처리되었습니다."))
                .when(itemGiveService).giveItemToStudent(eq(itemId), eq(2L), any());

        // when & then
        mockMvc.perform(post("/items/{itemId}/give", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 물품은 이미 지급 처리되었습니다."));
    }

    @Test
    void 물품_지급_실패_물품_없음() throws Exception {
        // given
        Long itemId = 999L;
        String requestBody = "{\"student_id\": 2}";

        doThrow(new IllegalArgumentException("해당 물품을 찾을 수 없습니다."))
                .when(itemGiveService).giveItemToStudent(eq(itemId), eq(2L), any());

        // when & then
        mockMvc.perform(post("/items/{itemId}/give", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 물품을 찾을 수 없습니다."));
    }

    @Test
    void 물품_지급_실패_ADMIN_권한_없음() throws Exception {
        // given
        Long itemId = 1L;
        String requestBody = "{\"student_id\": 2}";

        doThrow(new AccessDeniedException("ADMIN 권한이 없습니다."))
                .when(itemGiveService).giveItemToStudent(eq(itemId), eq(2L), any());

        // when & then
        mockMvc.perform(post("/items/{itemId}/give", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("ADMIN 권한이 없습니다."));
    }

    @Test
    void 물품_지급_실패_요청_데이터_검증_실패() throws Exception {
        // given
        Long itemId = 1L;
        String requestBody = "{}"; // student_id가 없는 요청

        // when & then
        mockMvc.perform(post("/items/{itemId}/give", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}