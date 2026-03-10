package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.comment.CommentResponseDisplay;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các thao tác liên quan đến Comment.
 *
 * Base URL: /api/comments
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // =========================================================================
    // ĐĂNG COMMENT
    // =========================================================================

    /**
     * [Chức năng] Đăng comment vào một bài viết.
     *
     * API: POST /api/comments/{postId}
     * Yêu cầu đăng nhập: Có
     *
     * @param postId        ID của bài viết cần comment
     * @param content       Nội dung comment (gửi qua request param)
     * @param userPrincipal Người đang đăng nhập (tự động inject từ JWT)
     * @return 200 OK nếu thành công, 404 nếu bài viết không tồn tại
     *
     * Ví dụ: POST /api/comments/5?content=Bài viết hay quá!
     */
    @PostMapping("/{postId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("[CommentController] addComment – postId={}, user={}",
                postId, userPrincipal.getUsername());

        commentService.comment(postId, content, userPrincipal.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Đăng comment thành công")
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

    @GetMapping("/status/{postId}")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<CommentResponseDisplay> comments = commentService.getCommentsByPostId(postId, userPrincipal.getId());

        return ResponseEntity.ok(
                ApiResponse.<List<CommentResponseDisplay>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy comments thành công")
                        .data(comments)
                        .build()
        );
    }
}
