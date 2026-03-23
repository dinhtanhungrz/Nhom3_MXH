package com.be_mxh.controller;

import com.be_mxh.dto.chat.RealtimeChatErrorResponse;
import com.be_mxh.dto.chat.RealtimeChatMessageRequest;
import com.be_mxh.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatRealtimeController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload RealtimeChatMessageRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = resolveUsername(principal, headerAccessor);
        if (username == null) {
            throw new IllegalStateException("Unauthorized");
        }

        messageService.sendMessage(request.getGroupId(), request.getContent(), username);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/chat-errors")
    public RealtimeChatErrorResponse handleRealtimeError(
            Throwable error,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = resolveUsername(principal, headerAccessor);
        String message = error.getMessage() == null ? "Khong gui duoc tin nhan realtime" : error.getMessage();

        if (username != null) {
            log.warn("Realtime chat error for {}: {}", username, message);
        } else {
            log.warn("Realtime chat error without authenticated user: {}", message);
        }

        return RealtimeChatErrorResponse.builder()
                .message(message)
                .build();
    }

    private String resolveUsername(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }

        Principal accessorUser = headerAccessor != null ? headerAccessor.getUser() : null;
        if (accessorUser != null && accessorUser.getName() != null && !accessorUser.getName().isBlank()) {
            return accessorUser.getName();
        }

        Object username = headerAccessor != null && headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get("username")
                : null;

        return username instanceof String value && !value.isBlank() ? value : null;
    }
}
