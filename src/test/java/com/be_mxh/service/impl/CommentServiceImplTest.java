package com.be_mxh.service.impl;

import com.be_mxh.entity.Comment;
import com.be_mxh.entity.User;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.CommentRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CommentServiceImpl – chức năng XÓA COMMENT
 *
 * Không cần Spring Context (chạy nhanh với MockitoExtension).
 * Mỗi test case log ra console để theo dõi luồng thực thi.
 *
 * Cách chạy:
 *   mvn test -Dtest=CommentServiceImplTest -q
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl – Xóa Comment Tests")
class CommentServiceImplTest {

    // ===========================================================
    // MOCK DEPENDENCIES
    // ===========================================================
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    // ===========================================================
    // DỮ LIỆU TEST DÙNG CHUNG
    // ===========================================================
    private User owner;          // Chủ comment (userId = 1)
    private User otherUser;      // Người lạ (userId = 2)
    private Comment comment;     // Comment chưa bị xóa (commentId = 10)

    @BeforeEach
    void setUp() {
        // Khởi tạo user chủ comment
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");

        // Khởi tạo user không phải chủ
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("bob");

        // Khởi tạo comment chưa bị xóa
        comment = new Comment();
        comment.setId(10L);
        comment.setContent("Bình luận test");
        comment.setUser(owner);        // Alice là chủ
        comment.setDeleted(false);
    }

    // ===========================================================
    // TEST CASE 1: XÓA THÀNH CÔNG
    // ===========================================================

    @Test
    @DisplayName("✅ TC01 – Chủ comment xóa chính comment của mình → thành công")
    void deleteComment_success_whenOwnerDeletes() {
        // ---- GIVEN ----
        // Repository tìm thấy comment (deleted = false)
        when(commentRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(comment));

        System.out.println("\n[TEST LOG] TC01: Owner (userId=1) xóa comment ID=10");

        // ---- WHEN ----
        // Gọi service với đúng owner
        commentService.deleteComment(10L, 1L);

        // ---- THEN ----
        // Flag deleted phải được bật lên true
        assertThat(comment.isDeleted())
                .as("Comment phải được đặt deleted = true sau khi xóa")
                .isTrue();

        // Phải gọi save() để lưu thay đổi xuống DB
        verify(commentRepository, times(1)).save(comment);

        System.out.println("[TEST LOG] TC01 PASS: comment.deleted=" + comment.isDeleted());
    }

    // ===========================================================
    // TEST CASE 2: KHÔNG PHẢI CHỦ → 403 ACCESS DENIED
    // ===========================================================

    @Test
    @DisplayName("🚫 TC02 – Người khác cố xóa comment không phải của mình → AccessDeniedException (403)")
    void deleteComment_throwsAccessDenied_whenNotOwner() {
        // ---- GIVEN ----
        when(commentRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.of(comment));

        System.out.println("\n[TEST LOG] TC02: Bob (userId=2) cố xóa comment của Alice (userId=1)");

        // ---- WHEN & THEN ----
        assertThatThrownBy(() -> commentService.deleteComment(10L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không có quyền xóa");

        // Không được gọi save() vì request bị từ chối
        verify(commentRepository, never()).save(any());

        System.out.println("[TEST LOG] TC02 PASS: AccessDeniedException ném đúng, save() không được gọi");
    }

    // ===========================================================
    // TEST CASE 3: COMMENT KHÔNG TỒN TẠI → 404 NOT FOUND
    // ===========================================================

    @Test
    @DisplayName("❌ TC03 – Comment không tồn tại hoặc đã bị xóa → ResourceNotFoundException (404)")
    void deleteComment_throwsNotFound_whenCommentDoesNotExist() {
        // ---- GIVEN ----
        // Giả lập comment không tồn tại trong DB (hoặc deleted = true)
        when(commentRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        System.out.println("\n[TEST LOG] TC03: Xóa comment ID=99 (không tồn tại)");

        // ---- WHEN & THEN ----
        assertThatThrownBy(() -> commentService.deleteComment(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("không tồn tại");

        // Không được gọi save()
        verify(commentRepository, never()).save(any());

        System.out.println("[TEST LOG] TC03 PASS: ResourceNotFoundException ném đúng");
    }

    // ===========================================================
    // TEST CASE 4: XÓA COMMENT ĐÃ BỊ XÓA TRƯỚC ĐÓ → 404
    // ===========================================================

    @Test
    @DisplayName("❌ TC04 – Xóa lần 2 comment đã bị xóa → ResourceNotFoundException (404)")
    void deleteComment_throwsNotFound_whenAlreadyDeleted() {
        // ---- GIVEN ----
        // Comment đã deleted = true → findByIdAndDeletedFalse trả về empty
        comment.setDeleted(true);
        when(commentRepository.findByIdAndDeletedFalse(10L))
                .thenReturn(Optional.empty());

        System.out.println("\n[TEST LOG] TC04: Xóa comment ID=10 đã bị xóa trước đó");

        // ---- WHEN & THEN ----
        assertThatThrownBy(() -> commentService.deleteComment(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());

        System.out.println("[TEST LOG] TC04 PASS: Không cho phép xóa 2 lần");
    }
}
