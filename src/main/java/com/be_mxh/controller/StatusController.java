package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.status.StatusResponseDisplay;
import com.be_mxh.dto.user.ProfileResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
         * [Chức năng] Tạo status mới (có thể kèm nhiều ảnh)
         *
         * API: POST /api/statuses
         * Content-Type: multipart/form-data
         *
         * @param content    Nội dung bài viết (không bắt buộc nếu có ảnh)
         * @param visibility Quyền hiển thị: PUBLIC | FRIENDS_ONLY | ONLY_ME (mặc định: PUBLIC)
         * @param images     Danh sách ảnh đính kèm (không bắt buộc, tối đa 10 ảnh)
         * @return Status vừa tạo kèm URL ảnh đã upload
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
         * [Chức năng] Lấy News Feed — danh sách bài viết hiển thị trên trang chủ
         *
         * API: GET /api/statuses
         * Yêu cầu đăng nhập: Có
         *
         * Lọc: chỉ trả về bài PUBLIC và FRIENDS_ONLY của bạn bè,
         *       hoặc tất cả bài của chính mình.
         *
         * @return Danh sách Status sắp xếp theo thời gian mới nhất
         */
        @GetMapping
        public ResponseEntity<?> getStatuses(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                List<StatusResponseDisplay> statuses = statusService.getFeedStatuses(userPrincipal.getId());

                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<List<StatusResponseDisplay>>builder()
                                .code(HttpStatus.OK.value())
                                .message("Get feed statuses successfully")
                                .data(statuses)
                                .build()
                );
        }

        /**
         * [Chức năng] Xem chi tiết một bài viết (có kiểm tra quyền xem)
         *
         * API: GET /api/statuses/{id}
         * Yêu cầu đăng nhập: Có
         *
         * @param id ID của status cần xem
         * @return Chi tiết Status nếu có quyền xem, lỗi 403 nếu không
         */
        @GetMapping("/{id}")
        public ResponseEntity<Status> getStatusById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                Status status = statusService.getStatusById(id, userPrincipal.getId());
                return ResponseEntity.ok(status);
        }

        /**
         * [Chức năng] Xóa bài viết (chỉ chủ bài viết được xóa)
         *
         * API: DELETE /api/statuses/{id}
         * Yêu cầu đăng nhập: Có
         *
         * @param id ID của status cần xóa
         * @return Thông báo xóa thành công, lỗi 403 nếu không phải chủ bài
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteStatus(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                statusService.deleteStatus(id, userPrincipal.getId());
                return ResponseEntity.ok("Xoá status thành công");
        }

        /**
         * [Chức năng] Tìm kiếm bài viết toàn mạng theo từ khóa — dùng cho trang "Kết quả tìm kiếm > tab Bài viết"
         *
         * API: GET /api/statuses/query?query={từ_khóa}
         * Yêu cầu đăng nhập: Có
         *
         * Tìm kiếm gần đúng (LIKE) trên toàn bộ bài viết, có lọc theo quyền hiển thị:
         *   - PUBLIC     → ai cũng tìm thấy
         *   - FRIENDS_ONLY → chỉ bạn bè tìm thấy
         *   - ONLY_ME    → chỉ chủ bài tìm thấy khi search chính mình
         *
         * @param query      Từ khóa cần tìm
         * @param currentUser Người đang đăng nhập (dùng để lọc quyền xem)
         * @return Danh sách StatusResponseDisplay khớp từ khóa, sắp xếp mới nhất trước
         *
         * Ví dụ: GET /api/statuses/query?query=hello
         */
        @GetMapping("/query")
        public ResponseEntity<?> query(
                        @RequestParam("query") String query,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                List<StatusResponseDisplay> results = statusService.findAllByContentContaining(query, currentUser.getId());
                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<List<StatusResponseDisplay>>builder()
                                .code(HttpStatus.OK.value())
                                .message("Get statuses search successfully")
                                .data(results)
                                .build()
                );
        }

        /**
         * [Chức năng] Xem tất cả bài viết hiển thị được của một người dùng cụ thể
         *
         * API: GET /api/statuses/user/{ownerId}
         * Yêu cầu đăng nhập: Có
         *
         * Lọc theo quyền hiển thị dựa vào quan hệ giữa người xem và chủ trang:
         *   - Chủ trang            → thấy tất cả (PUBLIC + FRIENDS_ONLY + ONLY_ME)
         *   - Bạn bè               → thấy PUBLIC + FRIENDS_ONLY
         *   - Người lạ / chưa đăng nhập → chỉ thấy PUBLIC
         *
         * @param ownerId    ID của người dùng cần xem trang cá nhân
         * @param currentUser Người đang đăng nhập (viewer)
         * @return Danh sách Status được phép xem, sắp xếp mới nhất trước
         *
         * Ví dụ: GET /api/statuses/user/5
         */
        @GetMapping("/user/{ownerId}")
        public ResponseEntity<?> getUserStatuses(
                        @PathVariable Long ownerId,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                Long viewerId = currentUser.getId();
                List<StatusResponseDisplay> statuses = statusService.getVisibleStatuses(ownerId, viewerId);
                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<List<StatusResponseDisplay>>builder()
                                .code(HttpStatus.OK.value())
                                .message("Get user statuses successfully")
                                .data(statuses)
                                .build()
                );
        }

        /**
         * [Chức năng] Tìm kiếm bài viết của một người cụ thể theo từ khóa — dùng cho thanh search trên trang cá nhân
         *
         * API: GET /api/statuses/user/{ownerId}/search?q={từ_khóa}
         * Yêu cầu đăng nhập: Có
         *
         * Kết hợp tìm kiếm gần đúng (LIKE) với lọc quyền hiển thị,
         * chỉ trả về bài viết của {ownerId} mà người xem có quyền thấy.
         *
         * @param ownerId    ID của người dùng cần tìm trong bài viết của họ
         * @param keyword    Từ khóa cần tìm
         * @param currentUser Người đang đăng nhập (viewer)
         * @return Danh sách StatusResponseDisplay khớp từ khóa và đủ quyền xem
         *
         * Ví dụ: GET /api/statuses/user/5/search?q=hello
         */
        @GetMapping("/user/{ownerId}/search")
        public ResponseEntity<?> searchUserStatuses(
                        @PathVariable Long ownerId,
                        @RequestParam("q") String keyword,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                Long viewerId = currentUser.getId();
                List<StatusResponseDisplay> results = statusService.searchUserStatuses(ownerId, viewerId, keyword);
                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<List<StatusResponseDisplay>>builder()
                                .code(HttpStatus.OK.value())
                                .message("Get statuses search successfully")
                                .data(results)
                                .build()
                );
        }

        /**
         * [Chức năng] Thay đổi quyền hiển thị của một bài viết (chỉ chủ bài viết được thay đổi)
         *
         * API: PATCH /api/statuses/{id}/visibility?visibility={giá_trị}
         * Yêu cầu đăng nhập: Có
         *
         * @param id            ID của status cần thay đổi
         * @param visibilityStr Quyền hiển thị mới: PUBLIC | FRIENDS_ONLY | ONLY_ME (không phân biệt hoa thường)
         * @return Thông báo thành công, lỗi 400 nếu visibility không hợp lệ, lỗi 403 nếu không phải chủ bài
         *
         * Ví dụ: PATCH /api/statuses/3/visibility?visibility=ONLY_ME
         */
        @PatchMapping("/{id}/visibility")
        public ResponseEntity<?> updateVisibility(
                @PathVariable Long id,
                @RequestParam("visibility") String visibilityStr,
                @AuthenticationPrincipal UserPrincipal userPrincipal) {

                // Chuyển string từ request ("only_me" hoặc "PUBLIC") sang Enum hợp lệ
                Status.Visibility newVis;
                try {
                        newVis = Status.Visibility.valueOf(visibilityStr.toUpperCase());
                } catch (IllegalArgumentException ex) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ApiResponse.<String>builder()
                                        .code(HttpStatus.BAD_REQUEST.value())
                                        .message("Visibility không hợp lệ. Vui lòng dùng: PUBLIC, FRIENDS_ONLY hoặc ONLY_ME")
                                        .build()
                        );
                }

                // Gọi Service thực thi logic cập nhật DB
                statusService.updateVisibility(id, newVis, userPrincipal.getId());

                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<String>builder()
                                .code(HttpStatus.OK.value())
                                .message("Cập nhật quyền hiển thị thành công")
                                .build()
                );
        }

        

}
