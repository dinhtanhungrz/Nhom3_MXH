package com.be_mxh.controller;

import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.service.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatusService statusService;

    /**
     * ✅ TEST 1: GET /api/v1/statuses/user/1/public - Valid Request
     */
    @Test
    void testGetPublicStatuses_ValidRequest_Success() throws Exception {
        // Arrange
        StatusResponse statusResponse = StatusResponse.builder()
                .id(1L)
                .username("test_user")
                .content("Test content")
                .visibility("PUBLIC")
                .createdAt(LocalDateTime.now())
                .imageUrls(List.of())
                .build();

        Page<StatusResponse> page = new PageImpl<>(List.of(statusResponse));

        when(statusService.getPublicStatusesByUser(eq(1L), eq(0), eq(10)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/statuses/user/1/public")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("test_user"))
                .andExpect(jsonPath("$.content[0].content").value("Test content"));
    }

    /**
     * ✅ TEST 2: GET with negative page - Bad Request
     */
    @Test
    void testGetPublicStatuses_NegativePage_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/statuses/user/1/public")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    /**
     * ✅ TEST 3: GET with size > 100 - Bad Request
     */
    @Test
    void testGetPublicStatuses_SizeTooLarge_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/statuses/user/1/public")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    /**
     * ✅ TEST 4: GET with default parameters
     */
    @Test
    void testGetPublicStatuses_DefaultParams_Success() throws Exception {
        Page<StatusResponse> page = new PageImpl<>(List.of());
        when(statusService.getPublicStatusesByUser(eq(1L), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/statuses/user/1/public"))
                .andExpect(status().isOk());
    }
}