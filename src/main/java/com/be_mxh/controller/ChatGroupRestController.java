package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.chat.ChatGroupResponse;
import com.be_mxh.dto.chat.CreateChatGroupRequest;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.ChatGroupService;
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

import java.util.List;

@RestController
@RequestMapping("/api/chat-groups")
@RequiredArgsConstructor
@Slf4j
public class ChatGroupRestController {

    private final ChatGroupService chatGroupService;

    /**
     * Táº¡o nhÃ³m chat má»›i
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createChatGroup(
            @RequestBody CreateChatGroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            ChatGroupResponse response = chatGroupService.createChatGroup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .code(HttpStatus.CREATED.value())
                            .message("NhÃ³m chat Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng")
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
     * Láº¥y danh sÃ¡ch nhÃ³m chat cá»§a ngÆ°á»i dÃ¹ng hiá»‡n táº¡i
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<?> getUserChatGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatGroupResponse> groups = chatGroupService.getUserChatGroups(pageable);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Láº¥y danh sÃ¡ch nhÃ³m chat thÃ nh cÃ´ng")
                        .data(groups)
                        .build()
        );
    }

    /**
     * Láº¥y thÃ´ng tin chi tiáº¿t nhÃ³m chat
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getChatGroupById(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            ChatGroupResponse response = chatGroupService.getChatGroupById(groupId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("Láº¥y thÃ´ng tin nhÃ³m chat thÃ nh cÃ´ng")
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
     * ThÃªm thÃ nh viÃªn vÃ o nhÃ³m
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMembersToGroup(
            @PathVariable Long groupId,
            @RequestBody java.util.Map<String, List<Long>> requestBody,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            List<Long> memberIds = requestBody.get("memberIds");
            ChatGroupResponse response = chatGroupService.addMembersToGroup(groupId, memberIds);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("ThÃªm thÃ nh viÃªn vÃ o nhÃ³m thÃ nh cÃ´ng")
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
     * XÃ³a thÃ nh viÃªn khá»i nhÃ³m
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMemberFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            chatGroupService.removeMemberFromGroup(groupId, memberId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("XÃ³a thÃ nh viÃªn khá»i nhÃ³m thÃ nh cÃ´ng")
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
     * XÃ³a nhÃ³m chat
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteChatGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            chatGroupService.deleteChatGroup(groupId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .message("XÃ³a nhÃ³m chat thÃ nh cÃ´ng")
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
