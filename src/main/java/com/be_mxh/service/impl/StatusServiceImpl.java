package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusImageResponse;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.dto.status.StatusResponseDisplay;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.repository.*;
import com.be_mxh.service.ImageUploadService;
import com.be_mxh.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.security.access.AccessDeniedException;
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

  @Override
  public void deleteStatus(Long statusId, Long userId) {

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

  // mapper
  private List<StatusResponseDisplay> mapStatusResponseDisplays(List<Status> statuses, Long viewerId) {
    List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
    for (Status status : statuses) {
      List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
      long totalLikes = likeRepository.countByStatusId(status.getId());
      long totalComments = commentRepository.countByStatusIdAndDeletedFalse(status.getId());
      boolean isLike = likeRepository.existsByStatusIdAndUserId(status.getId(), viewerId);
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
    boolean canComment = viewerId.equals(status.getUser().getId()) ||
      friendshipRepository.existsAcceptedFriendship(status.getUser().getId(), viewerId);

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
}
