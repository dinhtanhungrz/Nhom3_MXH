package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.status.*;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.*;
import com.be_mxh.service.ImageUploadService;
import com.be_mxh.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusServiceImpl implements StatusService {

  private final StatusRepository statusRepository;
  private final StatusImageRepository statusImageRepository;
  private final ImageUploadService imageUploadService;
  private final UserRepository userRepository;
  private final FriendshipRepository friendshipRepository;
  private final LikeRepository likeRepository;
  private final CommentRepository commentRepository;
  private final SecurityUtils securityUtils;

  /*
   * =========================
   * CREATE STATUS
   * =========================
   */
  @Transactional
  @Override
  public void createStatus(String content, String visibility, List<MultipartFile> images) {
    User user = securityUtils.getCurrentUser();

    Status status = Status.builder()
      .content(content)
      .user(user)
      .visibility(
        visibility.equals("ONLY_ME") ? Status.Visibility.ONLY_ME
          : visibility.equals("FRIENDS_ONLY") ? Status.Visibility.FRIENDS_ONLY
          : Status.Visibility.PUBLIC)
      .active(true)
      .build();

    // Lưu status trước để có ID
    Status result = statusRepository.save(status);

    // Upload ảnh nếu có
    if (images != null && !images.isEmpty()) {
      String folder = "statuses/" + status.getId();
      int sortOrder = 0;

      for (MultipartFile file : images) {
        if (file.isEmpty()) continue;

        ImageUploadResult uploadResult = imageUploadService.upload(file, folder);

        StatusImage statusImage = StatusImage.builder()
          .status(result)
          .url(uploadResult.getUrl())
          .publicId(uploadResult.getPublicId())
          .sortOrder(sortOrder++)
          .build();

        statusImageRepository.save(statusImage);
      }
    }
  }

  @Override
  public List<StatusResponse> getStatusesByProfile() {
    Long userId = securityUtils.getCurrentUserId();
    List<Status> statuses = statusRepository.findStatusByUserId(userId);

    List<StatusResponse> responses = new ArrayList<>();
    for (Status status : statuses) {
      Long likeCount = likeRepository.countByStatusId(status.getId());
      Long commentCount = commentRepository.countByStatusIdAndDeletedFalse(status.getId());
      List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
      boolean isLike = likeRepository.existsByStatusIdAndUserId(status.getId(), userId);
      responses.add(mapStatusResponse(status, images, commentCount, likeCount, isLike));
    }
    return responses;
  }

  @Override
  public List<StatusResponseDisplay> getFeedStatuses(Long viewerId) {
    List<Status> results = statusRepository.getNewsfeedStatuses(viewerId);
    return mapStatusResponseDisplays(results, viewerId);
  }

  @Override
  public Status getStatusById(Long statusId, Long userId) {
    return null;
  }

  @Transactional
  @Override
  public void deleteStatus(Long statusId, Long currentUserId) {
    // 1. Tìm status
    Status status = statusRepository.findById(statusId)
      .orElseThrow(() -> new ResourceNotFoundException("Status không tồn tại"));

    // 2. Kiểm tra quyền
    if (!status.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Bạn không có quyền xóa bài viết này");
    }

    // 3. Xóa ảnh trên Cloudinary trước
    List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(statusId);

    for (StatusImage img : images) {
      imageUploadService.delete(img.getPublicId());
    }

    // 4. Xóa ảnh trong DB rồi xóa status
    statusImageRepository.deleteAll(images);
    statusImageRepository.flush();
    statusRepository.delete(status);
  }

  @Override
  public List<StatusResponseDisplay> getVisibleStatuses(Long ownerId, Long viewerId) {
    List<Status> results = statusRepository.findVisibleStatuses(ownerId, viewerId);
    return mapStatusResponseDisplays(results, viewerId);
  }

  @Override
  public List<StatusResponseDisplay> searchUserStatuses(Long ownerId, Long viewerId, String keyword) {
    List<Status> results = statusRepository.searchVisibleStatuses(ownerId, viewerId, keyword);
    return mapStatusResponseDisplays(results, viewerId);
  }

  @Override
  public List<StatusResponseDisplay> findAllByContentContaining(String query, Long viewerId) {
    List<Status> results = statusRepository.globalSearch(viewerId, query);
    return mapStatusResponseDisplays(results, viewerId);
  }

  @Transactional
  @Override
  public void updateVisibility(Long statusId, Status.Visibility newVisibility, Long currentUser_Id) {

    // 1. Tìm status trong DB
    Status status = statusRepository.findById(statusId)
      .orElseThrow(() -> new EntityNotFoundException("Status không tồn tại"));

    // 2. Kiểm tra xem người đang request có phải là CHỦ của status này không
    if (!status.getUser().getId().equals(currentUser_Id)) {
      throw new AccessDeniedException("Bạn không có quyền thay đổi quyền hiển thị của status này");
    }

    // 3. Cập nhật quyền mới và lưu lại
    status.setVisibility(newVisibility);
    statusRepository.save(status);
  }

  @Transactional
  @Override
  public void updateStatus(Long statusId, UpdateStatusRequest request,
                           List<MultipartFile> newImages, Long currentUserId) {

    // 1. Tìm status — throw ResourceNotFoundException nếu không tồn tại (handler 404 đã có)
    Status status = statusRepository.findById(statusId)
      .orElseThrow(() -> new ResourceNotFoundException("Status không tồn tại"));

    // 2. Kiểm tra quyền — throw AccessDeniedException (handler 403 đã có)
    if (!status.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Bạn không có quyền chỉnh sửa status này");
    }

    // 3. Validate content nếu có truyền vào
    if (request.getContent() != null && request.getContent().trim().isEmpty()) {
      throw new BadRequestException("Nội dung bài viết không được để trống");
    }

    if (request.getContent() != null && request.getContent().length() > 3000) {
      throw new BadRequestException("Nội dung bài viết không được vượt quá 3000 ký tự");
    }

    // 4. Validate visibility nếu có truyền vào
    if (request.getVisibility() != null) {
      try {
        status.setVisibility(
          Status.Visibility.valueOf(request.getVisibility().toUpperCase())
        );
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(
          "Visibility không hợp lệ. Vui lòng dùng: PUBLIC, FRIENDS_ONLY hoặc ONLY_ME"
        );
      }
    }

    // 5. Validate deleteImageIds nếu có truyền vào
    if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
      List<StatusImage> toDelete = statusImageRepository
        .findAllById(request.getDeleteImageIds());

      // Kiểm tra id ảnh có thuộc status này không
      List<Long> invalidIds = request.getDeleteImageIds().stream()
        .filter(id -> toDelete.stream()
          .noneMatch(img -> img.getId().equals(id)
            && img.getStatus().getId().equals(statusId)))
        .toList();

      if (!invalidIds.isEmpty()) {
        throw new BadRequestException(
          "Ảnh không tồn tại hoặc không thuộc bài viết này: " + invalidIds
        );
      }

      // Kiểm tra không được xóa hết ảnh nếu content cũng trống
      boolean contentWillBeEmpty = request.getContent() != null
        ? request.getContent().trim().isEmpty()
        : status.getContent() == null || status.getContent().trim().isEmpty();

      long remainingImages = statusImageRepository
        .findByStatusIdOrderBySortOrderAsc(statusId).size() - toDelete.size();

      boolean newImagesEmpty = newImages == null || newImages.isEmpty();

      if (contentWillBeEmpty && remainingImages == 0 && newImagesEmpty) {
        throw new BadRequestException("Bài viết phải có nội dung hoặc ít nhất một ảnh");
      }
    }

    // 6. Validate file ảnh mới nếu có
    if (newImages != null && !newImages.isEmpty()) {
      List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
      long maxSize = 10 * 1024 * 1024; // 10MB

      for (MultipartFile file : newImages) {
        if (file.isEmpty()) continue;

        if (!allowedTypes.contains(file.getContentType())) {
          throw new BadRequestException(
            "File '" + file.getOriginalFilename() + "' không đúng định dạng. Chỉ chấp nhận: jpg, png, webp, gif"
          );
        }

        if (file.getSize() > maxSize) {
          throw new BadRequestException(
            "File '" + file.getOriginalFilename() + "' vượt quá dung lượng tối đa 10MB"
          );
        }
      }
    }

    // ── Sau khi validate xong mới thực hiện thay đổi ──

    if (request.getContent() != null) {
      status.setContent(request.getContent().trim());
    }

    statusRepository.save(status);

    // Xóa ảnh cũ
    if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
      List<StatusImage> toDelete = statusImageRepository.findAllById(request.getDeleteImageIds());
      for (StatusImage img : toDelete) {
        imageUploadService.deleteByPublicId(img.getPublicId());
        statusImageRepository.delete(img);
      }
    }

    // Upload ảnh mới
    if (newImages != null && !newImages.isEmpty()) {
      int nextSortOrder = statusImageRepository
        .findByStatusIdOrderBySortOrderAsc(statusId)
        .stream()
        .mapToInt(StatusImage::getSortOrder)
        .max()
        .orElse(-1) + 1;

      String folder = "statuses/" + statusId;

      for (MultipartFile file : newImages) {
        if (file.isEmpty()) continue;

        ImageUploadResult uploadResult = imageUploadService.upload(file, folder);

        statusImageRepository.save(StatusImage.builder()
          .status(status)
          .url(uploadResult.getUrl())
          .publicId(uploadResult.getPublicId())
          .sortOrder(nextSortOrder++)
          .build());
      }
    }
  }

  // mapper
  private List<StatusResponseDisplay> mapStatusResponseDisplays(List<Status> statuses, Long viewerId) {
    List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
    for (Status status : statuses) {
      List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
      long totalLikes = likeRepository.countByStatusId(status.getId());
      long totalComments = commentRepository.countByStatusIdAndDeletedFalse(status.getId());
      boolean isLike = viewerId != null && likeRepository.existsByStatusIdAndUserId(status.getId(), viewerId);
      statusResponseDisplays.add(mapStatusResponseDisplay(status, images, totalComments, totalLikes, viewerId, isLike));
    }
    return statusResponseDisplays;
  }

  private StatusResponseDisplay mapStatusResponseDisplay(
    Status status,
    List<StatusImage> images,
    long totalComments,
    long totalLikes,
    Long viewerId,
    boolean isLike
  ) {
    boolean canComment = viewerId != null &&(viewerId.equals(status.getUser().getId()) ||
      friendshipRepository.existsAcceptedFriendship(status.getUser().getId(), viewerId));

    return StatusResponseDisplay.builder()
      .id(status.getId())
      .content(status.getContent())
      .visibility(status.getVisibility().name())
      .isActive(status.isActive())
      .createdAt(status.getCreatedAt())
      .updatedAt(status.getUpdatedAt())
      .likesCount(totalLikes)
      .commentsCount(totalComments)
      .canComment(canComment)
      .authorId(status.getUser().getId())
      .authorName(status.getUser().getFullName() != null ? status.getUser().getFullName() : status.getUser().getUsername())
      .authorAvatarUrl(status.getUser().getAvatarUrl())
      .isLike(isLike)
      .imageUrls(images.stream().map(this::mapToImageUrl).toList())
      .build();
  }

  private StatusImageResponse mapToImageUrl(StatusImage img) {
    return StatusImageResponse.builder()
      .id(img.getId())
      .url(img.getUrl())
      .sortOrder(img.getSortOrder())
      .build();
  }

  private StatusResponse mapStatusResponse(Status status, List<StatusImage> images, Long totalComments, Long totalLikes, boolean isLike) {
    return StatusResponse.builder()
      .id(status.getId())
      .content(status.getContent())
      .visibility(status.getVisibility().name())
      .isActive(status.isActive())
      .createdAt(status.getCreatedAt())
      .updatedAt(status.getUpdatedAt())
      .likesCount(totalLikes)
      .commentsCount(totalComments)
      .isLike(isLike)
      .imageUrls(images.stream().map(this::mapToImageUrl).toList())
      .build();
  }
  @Override
  public List<StatusResponseDisplay> getGuestFeed(){
    List<Status> results = statusRepository.findGuestFeed();
    return mapStatusResponseDisplays(results, null);
  }
  private List<StatusResponseDisplay> mapStatusResponseDisplaysGuest(List<Status> statuses) {
    List<StatusResponseDisplay> result = new ArrayList<>();

    for (Status status : statuses) {
      List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());

      long totalLikes = likeRepository.countByStatusId(status.getId());
      long totalComments = commentRepository.countByStatusIdAndDeletedFalse(status.getId());

      result.add(StatusResponseDisplay.builder()
              .id(status.getId())
              .content(status.getContent())
              .visibility(status.getVisibility().name())
              .createdAt(status.getCreatedAt())
              .likesCount(totalLikes)
              .commentsCount(totalComments)
              .isLike(false)
              .canComment(false)

              .authorId(status.getUser().getId())
              .authorName(status.getUser().getFullName())
              .authorAvatarUrl(status.getUser().getAvatarUrl())

              .imageUrls(images.stream().map(this::mapToImageUrl).toList())
              .build());
    }

    return result;
  }
}
