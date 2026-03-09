package com.be_mxh.controller;

import com.be_mxh.dto.status.LikeStatus;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/status-like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * LIKE STATUS
     */
    @PostMapping("/{statusId}")
    public ResponseEntity<LikeStatus> likeStatus(
            @PathVariable Long statusId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        LikeStatus result =
                likeService.likeStatus(statusId, user.getId());

        return ResponseEntity.ok(result);
    }

    /**
     * UNLIKE STATUS
     */
    @DeleteMapping("/{statusId}")
    public ResponseEntity<LikeStatus> unlikeStatus(
            @PathVariable Long statusId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        LikeStatus result =
                likeService.unlikeStatus(statusId, user.getId());

        return ResponseEntity.ok(result);
    }

    /**
     * GET LIKE STATUS
     */
    @GetMapping("/{statusId}")
    public ResponseEntity<LikeStatus> getLikeStatus(
            @PathVariable Long statusId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        LikeStatus result =
                likeService.getLikeStatus(statusId, user.getId());

        return ResponseEntity.ok(result);
    }
}
