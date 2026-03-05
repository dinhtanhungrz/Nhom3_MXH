package com.be_mxh.controller;

import com.be_mxh.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comments/{commentId}/likes")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<?> likeComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        commentLikeService.likeComment(commentId, username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã like comment");
        response.put("likeCount", commentLikeService.getLikeCount(commentId));
        response.put("isLiked", true);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<?> unlikeComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        commentLikeService.unlikeComment(commentId, username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã bỏ like comment");
        response.put("likeCount", commentLikeService.getLikeCount(commentId));
        response.put("isLiked", false);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long commentId) {
        long count = commentLikeService.getLikeCount(commentId);
        return ResponseEntity.ok(Map.of("likeCount", count));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean isLiked = commentLikeService.isCommentLikedByUser(commentId, username);
        long likeCount = commentLikeService.getLikeCount(commentId);

        return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "likeCount", likeCount
        ));
    }
}
