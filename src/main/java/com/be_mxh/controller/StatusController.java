package com.be_mxh.controller;

import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import lombok.RequiredArgsConstructor;
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
        public ResponseEntity<?> createStatus(
                        @RequestParam(value = "content", required = false) String content,
                        @RequestParam(value = "visibility", required = false, defaultValue = "PUBLIC") String visibility,
                        @RequestParam(value = "images", required = false) List<MultipartFile> images,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {

                if ((content == null || content.trim().isEmpty())
                                && (images == null || images.isEmpty())) {
                        return ResponseEntity.badRequest()
                                        .body("Status phải có nội dung hoặc ảnh");
                }

                // Chuyển chuỗi "PUBLIC"/"FRIENDS_ONLY"/"ONLY_ME" sang enum
                Status.Visibility vis;
                try {
                        vis = Status.Visibility.valueOf(visibility.toUpperCase());
                } catch (IllegalArgumentException ex) {
                        return ResponseEntity.badRequest()
                                        .body("Visibility không hợp lệ. Dùng: PUBLIC, FRIENDS_ONLY hoặc ONLY_ME");
                }

                // Gọi overload mới (có hỗ trợ visibility)
                com.be_mxh.dto.status.CreateStatusRequest request = new com.be_mxh.dto.status.CreateStatusRequest();
                request.setContent(content);
                request.setVisibility(vis);

                return ResponseEntity.ok(statusService.createStatus(request, images, userPrincipal));
        }

        /**
         * Lấy danh sách status (news feed)
         */
        @GetMapping
        public ResponseEntity<List<Status>> getStatuses(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                List<Status> statuses = statusService.getFeedStatuses(userPrincipal.getId());

                return ResponseEntity.ok(statuses);
        }

        /**
         * Lấy chi tiết 1 status
         */
        @GetMapping("/{id}")
        public ResponseEntity<Status> getStatusById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                Status status = statusService.getStatusById(id, userPrincipal.getId());
                return ResponseEntity.ok(status);
        }

        /**
         * Xoá status
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteStatus(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                statusService.deleteStatus(id, userPrincipal.getId());
                return ResponseEntity.ok("Xoá status thành công");
        }

        /**
         * Tìm kiếm toàn bộ status trên mạng theo từ khóa
         */
        @GetMapping("/query")
        public ResponseEntity<?> query(@RequestParam("query") String query) {
                List<Status> statuses = statusService.findAllByContentContaining(query);
                return ResponseEntity.ok(statuses);
        }

        /**
         * Lấy tất cả status hiển thị được của một người (theo quyền xem)
         */
        @GetMapping("/user/{ownerId}")
        public ResponseEntity<List<Status>> getUserStatuses(
                        @PathVariable Long ownerId,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                Long viewerId = currentUser.getId();
                return ResponseEntity.ok(statusService.getVisibleStatuses(ownerId, viewerId));
        }

        /**
         * Tìm kiếm tương đối status của một người cụ thể theo từ khóa
         * GET /api/statuses/user/{ownerId}/search?q=...
         */
        @GetMapping("/user/{ownerId}/search")
        public ResponseEntity<List<Status>> searchUserStatuses(
                        @PathVariable Long ownerId,
                        @RequestParam("q") String keyword,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                Long viewerId = currentUser.getId();
                List<Status> results = statusService.searchUserStatuses(ownerId, viewerId, keyword);
                return ResponseEntity.ok(results);
        }
}
