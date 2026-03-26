package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.dto.status.StatusResponseDisplay;
import com.be_mxh.dto.status.UpdateStatusRequest;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import lombok.RequiredArgsConstructor;
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
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @RequestParam(value = "images", required = false) List<MultipartFile> images
  ) {

    if ((content == null || content.trim().isEmpty())
      && (images == null || images.isEmpty())) {
      return ResponseEntity.badRequest().build();
    }

    statusService.createStatus(content, visibility, images);

    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.<Object>builder()
        .code(HttpStatus.OK.value())
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
   * [Chức năng] Lấy News Feed — danh sách bài viết hiển thị trên trang chủ
   * <p>
   * API: GET /api/statuses
   * Yêu cầu đăng nhập: Có
   * <p>
   * Lọc: chỉ trả về bài PUBLIC và FRIENDS_ONLY của bạn bè,
   * hoặc tất cả bài của chính mình.
   *
   * @return Danh sách Status sắp xếp theo thời gian mới nhất
   */
  @GetMapping
  public ResponseEntity<?> getFeedStatuses(
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
   * <p>
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
   * <p>
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

    return ResponseEntity.ok(
      ApiResponse.<Void>builder()
        .code(200)
        .message("Delete status successfully")
        .build()
    );
  }

  /**
   * [Chức năng] Tìm kiếm bài viết toàn mạng theo từ khóa — dùng cho trang "Kết quả tìm kiếm > tab Bài viết"
   * <p>
   * API: GET /api/statuses/query?query={từ_khóa}
   * Yêu cầu đăng nhập: Có
   * <p>
   * Tìm kiếm gần đúng (LIKE) trên toàn bộ bài viết, có lọc theo quyền hiển thị:
   * - PUBLIC     → ai cũng tìm thấy
   * - FRIENDS_ONLY → chỉ bạn bè tìm thấy
   * - ONLY_ME    → chỉ chủ bài tìm thấy khi search chính mình
   *
   * @param query       Từ khóa cần tìm
   * @param currentUser Người đang đăng nhập (dùng để lọc quyền xem)
   * @return Danh sách StatusResponseDisplay khớp từ khóa, sắp xếp mới nhất trước
   * <p>
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
   * <p>
   * API: GET /api/statuses/user/{ownerId}
   * Yêu cầu đăng nhập: Có
   * <p>
   * Lọc theo quyền hiển thị dựa vào quan hệ giữa người xem và chủ trang:
   * - Chủ trang            → thấy tất cả (PUBLIC + FRIENDS_ONLY + ONLY_ME)
   * - Bạn bè               → thấy PUBLIC + FRIENDS_ONLY
   * - Người lạ / chưa đăng nhập → chỉ thấy PUBLIC
   *
   * @param ownerId     ID của người dùng cần xem trang cá nhân
   * @param currentUser Người đang đăng nhập (viewer)
   * @return Danh sách Status được phép xem, sắp xếp mới nhất trước
   * <p>
   * Ví dụ: GET /api/statuses/user/5
   */
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @GetMapping("/user/{ownerId}")
  public ResponseEntity<?> getStatusesByUser(
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
   * <p>
   * API: GET /api/statuses/user/{ownerId}/search?q={từ_khóa}
   * Yêu cầu đăng nhập: Có
   * <p>
   * Kết hợp tìm kiếm gần đúng (LIKE) với lọc quyền hiển thị,
   * chỉ trả về bài viết của {ownerId} mà người xem có quyền thấy.
   *
   * @param ownerId     ID của người dùng cần tìm trong bài viết của họ
   * @param keyword     Từ khóa cần tìm
   * @param currentUser Người đang đăng nhập (viewer)
   * @return Danh sách StatusResponseDisplay khớp từ khóa và đủ quyền xem
   * <p>
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

  @PreAuthorize("hasRole('USER')")
  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> updateStatus(
    @PathVariable Long id,
    @RequestParam(value = "content", required = false) String content,
    @RequestParam(value = "visibility", required = false) String visibility,
    @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
    @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
    @AuthenticationPrincipal UserPrincipal userPrincipal) {

    UpdateStatusRequest request = UpdateStatusRequest.builder()
      .content(content)
      .visibility(visibility)
      .deleteImageIds(deleteImageIds)
      .build();

    statusService.updateStatus(id, request, newImages, userPrincipal.getId());

    return ResponseEntity.ok(
      ApiResponse.<Void>builder()
        .code(HttpStatus.OK.value())
        .message("Update status successfully")
        .build()
    );
  }
  @GetMapping("/guest")
  public ResponseEntity<?> getGuestFeed() {
    List<StatusResponseDisplay> statuses = statusService.getGuestFeed();

    return ResponseEntity.ok(
            ApiResponse.<List<StatusResponseDisplay>>builder()
                    .code(200)
                    .message("Get guest feed successfully")
                    .data(statuses)
                    .build()
    );
  }
}
