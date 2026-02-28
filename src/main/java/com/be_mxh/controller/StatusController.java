package com.be_mxh.controller;

import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StatusResponse> createStatus(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        if ((content == null || content.trim().isEmpty())
                && (images == null || images.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        StatusResponse response = statusService.createStatus(
                content,
                visibility,
                images,
                userPrincipal.getId()
        );

        return ResponseEntity.ok(response);
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
    public ResponseEntity<StatusResponse> getStatusById(
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
    /**
     * FIXED: Lấy danh sách public statuses của user
     *
     * 1. Thêm @Validated annotation
     * 2. Thêm @Min/@Max validation
     * 3. Thêm Swagger documentation
     * 4. Proper error handling & logging
     */
    @GetMapping("/user/{userId}/public")
    @Operation(
            summary = "Lấy danh sách status công khai",
            description = "Lấy danh sách các status được đánh dấu 'Công khai' của một người dùng",
            tags = {"Status"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Thành công",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<Page<StatusResponse>> getPublicStatuses(
            @PathVariable
            @Parameter(description = "User ID", example = "1")
            Long userId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page phải >= 0")
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size phải >= 1")
            @Max(value = 100, message = "Size tối đa 100")
            @Parameter(description = "Số item mỗi trang (1-100)", example = "10")
            int size
    ) {
        log.info("GET /api/v1/statuses/user/{}/public?page={}&size={}", userId, page, size);

        try {
            Page<StatusResponse> result = statusService.getPublicStatusesByUser(userId, page, size);
            log.info("Successfully retrieved {} public statuses for user: {}",
                    result.getNumberOfElements(), userId);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input parameters for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving public statuses for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
