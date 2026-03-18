package com.be_mxh.service;

import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.dto.status.StatusResponseDisplay;
import com.be_mxh.dto.status.UpdateStatusRequest;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StatusService {

  List<StatusResponse> getStatusesByProfile();

  /**
   * [Chức năng] Lấy danh sách bài viết trên News Feed của người dùng
   * API: GET /api/statuses
   * <p>
   * Trả về bài của bạn bè (PUBLIC + FRIENDS_ONLY) và tất cả bài của chính mình.
   *
   * @param userId ID người đang xem news feed
   * @return Danh sách Status sắp xếp mới nhất trước
   */
  List<StatusResponseDisplay> getFeedStatuses(Long userId);

  /**
   * [Chức năng] Lấy chi tiết 1 status — phiên bản cũ (không kiểm tra visibility)
   * API: GET /api/statuses/{id}
   *
   * @param statusId ID của status cần lấy
   * @param userId   ID người đang xem
   * @return Status entity
   */
  Status getStatusById(Long statusId, Long userId);

  /**
   * [Chức năng] Xóa mềm status — phiên bản cũ
   * API: DELETE /api/statuses/{id}
   *
   * @param statusId ID của status cần xóa
   * @param currentUserId   ID người thực hiện (phải là chủ bài)
   */
  void deleteStatus(Long statusId, Long currentUserId);

  /**
   * [Chức năng] Tạo status mới kèm nhiều ảnh và quyền hiển thị — phiên bản mới
   * API: POST /api/statuses
   * <p>
   * Hỗ trợ visibility (PUBLIC / FRIENDS_ONLY / ONLY_ME), upload ảnh lên Cloudinary,
   * rollback ảnh nếu lưu DB thất bại.
   *
   * @param request     DTO chứa content và visibility
   * @param images      Danh sách ảnh đính kèm (tối đa 10)
   * @param currentUser Người đang đăng nhập
   * @return StatusResponse DTO gồm id, content, visibility, imageUrls
   */
  void createStatus(String content, String visibility, List<MultipartFile> images);

  /**
   * [Chức năng] Tìm kiếm bài viết toàn mạng theo từ khóa — dùng cho trang "Kết quả tìm kiếm > tab Bài viết"
   * API: GET /api/statuses/query?query={từ_khóa}
   * <p>
   * Tìm gần đúng (LIKE) + lọc visibility so với viewerId:
   * - PUBLIC     → luôn hiện
   * - FRIENDS_ONLY → chỉ hiện nếu viewer là bạn bè chủ bài
   * - ONLY_ME   → chỉ hiện nếu viewer = chủ bài
   *
   * @param query    Từ khóa cần tìm
   * @param viewerId ID người đang đăng nhập (để lọc quyền xem)
   * @return Danh sách StatusResponseDisplay khớp từ khóa và đủ quyền xem
   */
  List<StatusResponseDisplay> findAllByContentContaining(String query, Long viewerId);

  /**
   * [Chức năng] Lấy tất cả bài viết hiển thị được của một người — dùng cho trang cá nhân
   * API: GET /api/statuses/user/{ownerId}
   * <p>
   * - Chủ trang  → thấy tất cả (PUBLIC + FRIENDS_ONLY + ONLY_ME)
   * - Bạn bè     → thấy PUBLIC + FRIENDS_ONLY
   * - Người lạ   → chỉ thấy PUBLIC
   *
   * @param ownerId  ID chủ trang cá nhân
   * @param viewerId ID người đang xem
   * @return Danh sách Status được phép xem, sắp xếp mới nhất trước
   */
  List<StatusResponseDisplay> getVisibleStatuses(Long ownerId, Long viewerId);

  /**
   * [Chức năng] Tìm kiếm bài viết của một người theo từ khóa — dùng cho thanh search trên trang cá nhân
   * API: GET /api/statuses/user/{ownerId}/search?q={từ_khóa}
   * <p>
   * Kết hợp tìm gần đúng (LIKE) + lọc visibility.
   * Nếu ownerId = viewerId (chính chủ tìm bài mình) → thấy kể cả ONLY_ME.
   *
   * @param ownerId  ID của người được tìm kiếm bài viết
   * @param viewerId ID người đang đăng nhập (viewer)
   * @param keyword  Từ khóa cần tìm
   * @return Danh sách StatusResponseDisplay khớp từ khóa và đủ quyền xem
   */
  List<StatusResponseDisplay> searchUserStatuses(Long ownerId, Long viewerId, String keyword);

  /**
   * [Chức năng] Thay đổi quyền hiển thị của một bài viết
   * API: PATCH /api/statuses/{id}/visibility?visibility={giá_trị}
   * <p>
   * Chỉ chủ bài mới được thay đổi, ném AccessDeniedException nếu không phải chủ.
   *
   * @param statusId      ID của status cần thay đổi
   * @param newVisibility Giá trị mới: PUBLIC | FRIENDS_ONLY | ONLY_ME
   * @param userId        ID người đang đăng nhập (phải là chủ bài)
   */
  void updateVisibility(Long statusId, Status.Visibility newVisibility, Long userId);

  void updateStatus(Long statusId, UpdateStatusRequest request, List<MultipartFile> newImages, Long currentUserId);
}
