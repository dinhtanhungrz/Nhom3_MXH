package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content cannot be empty");
        }

        commentService.createComment(request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Comment posted successfully")
                        .build()
        );
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<?> getCommentsByStatus(
            @PathVariable Long statusId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentUserId = (userPrincipal != null) ? userPrincipal.getId() : null;
        List<CommentResponse> responses = commentService.getCommentsByStatus(statusId, currentUserId);
        
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Get comments successfully")
                        .data(responses)
                        .build()
        );
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        commentService.updateComment(id, request.getContent(), userPrincipal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Comment updated successfully")
                        .build()
        );
    }
    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.likeComment(commentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment liked successfully"));
    }

    @DeleteMapping("/{commentId}/unlike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.unlikeComment(commentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment unliked successfully"));
    }

}
