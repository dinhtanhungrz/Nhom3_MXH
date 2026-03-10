package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    /**
     * Tạo status mới + upload nhiều ảnh
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStatus(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {

        if ((content == null || content.trim().isEmpty())
                && (images == null || images.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        statusService.createStatus(content, visibility, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Object>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Created status!")
                        .data(null)
                        .build()
        );
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getStatusesByProfile() {
        List<StatusResponse> statuses = statusService.getStatusesByProfile();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<StatusResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get statuses by profile successfully!")
                        .data(statuses)
                        .build()
        );
    }

    /**
     * Lấy danh sách status (news feed)
     */
    @GetMapping
    public ResponseEntity<List<StatusResponse>> getStatuses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                statusService.getFeedStatuses(userPrincipal.getId())
        );
    }

    /**
     * Lấy chi tiết 1 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStatusById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                statusService.getStatusById(id, userPrincipal.getId())
        );
    }

    /**
     * Xoá status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        statusService.deleteStatus(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<StatusResponse>> getPublicStatuses(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<StatusResponse> result = statusService.getPublicStatusesByUser(userId, page, size);
        return ResponseEntity.ok(result);
    }
}
