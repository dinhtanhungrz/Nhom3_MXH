package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
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


  // =========================================================================
  // XÓA COMMENT
  // =========================================================================

  /**
   * [Chức năng] Xóa comment của chính mình (soft-delete).
   *
   * API: DELETE /api/comments/{id}
   * Yêu cầu đăng nhập: Có
   *
   * Luồng xử lý:
   *  1. Xác thực JWT → lấy userId từ token
   *  2. Tìm comment theo ID (404 nếu không tồn tại hoặc đã bị xóa)
   *  3. So sánh userId với chủ comment (403 nếu không phải chủ)
   *  4. Đặt deleted = true (soft-delete, dữ liệu vẫn còn trong DB)
   *
   * Lưu ý: Nút "..." chỉ hiện phía FE khi comment thuộc user hiện tại.
   * Tuy nhiên BE vẫn kiểm tra ownership để đảm bảo bảo mật.
   *
   * @param id            ID của comment cần xóa
   * @param userPrincipal Người đang đăng nhập (tự động inject từ JWT)
   * @return 200 OK + message xác nhận
   *
   * Ví dụ: DELETE /api/comments/12
   *
   * HTTP Responses:
   *  - 200: Xóa thành công
   *  - 403: Không phải chủ comment (AccessDeniedException)
   *  - 404: Comment không tồn tại hoặc đã bị xóa (ResourceNotFoundException)
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(
    @PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal userPrincipal) {

    log.info("[CommentController] deleteComment – commentId={}, requestBy userId={}",
      id, userPrincipal.getId());

    commentService.deleteComment(id, userPrincipal.getId());

    log.info("[CommentController] ✅ Comment {} xóa thành công bởi userId={}", id, userPrincipal.getId());

    return ResponseEntity.ok(
      ApiResponse.<Void>builder()
        .code(HttpStatus.OK.value())
        .message("Xóa comment thành công")
        .build()
    );
  }
    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId, @AuthenticationPrincipal UserPrincipal currentUser) {
        // Gọi hàm mới
        boolean isLiked = commentService.toggleLikeComment(commentId, currentUser.getId());

        // Trả về JSON để Frontend nhận diện và đổi màu nút ngay lập tức
        return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "message", isLiked ? "Liked successfully" : "Unliked successfully"
        ));
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
