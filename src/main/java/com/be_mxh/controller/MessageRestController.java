package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.chat.MessageResponse;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageRestController {

    private final MessageService messageService;

    /**
     * Gửi tin nhắn
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            Long groupId = Long.parseLong(request.get("groupId").toString());
            String content = request.get("content").toString();

            MessageResponse response = messageService.sendMessage(groupId, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .code(HttpStatus.CREATED.value())
                            .message("Tin nhắn được gửi thành công")
                            .data(response)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Lấy danh sách tin nhắn của nhóm
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageResponse> messages = messageService.getGroupMessages(groupId, pageable);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("Lấy danh sách tin nhắn thành công")
                            .data(messages)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Chỉnh sửa tin nhắn
     */
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{messageId}")
    public ResponseEntity<?> editMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            String content = request.get("content");
            MessageResponse response = messageService.editMessage(messageId, content);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("Chỉnh sửa tin nhắn thành công")
                            .data(response)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Xóa tin nhắn
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            messageService.deleteMessage(messageId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("Xóa tin nhắn thành công")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
