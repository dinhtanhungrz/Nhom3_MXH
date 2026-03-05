package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusImageResponse;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.repository.*;
import com.be_mxh.service.ImageUploadService;
import com.be_mxh.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SecurityUtils securityUtils;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    /* =========================
       CREATE STATUS
       ========================= */

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
            Long commentCount = commentRepository.countByStatusId(status.getId());
            List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
            responses.add(mapStatusResponse(status, images, commentCount, likeCount));
        }
        return responses;
    }

    @Override
    public List<StatusResponse> getFeedStatuses(Long userId) {
        return List.of();
    }

    @Override
    public Status getStatusById(Long statusId, Long userId) {
        return null;
    }

    @Override
    public void deleteStatus(Long statusId, Long userId) {

    }

//
//    @Transactional
//    @Override
//    public StatusResponse createStatus(
//            CreateStatusRequest request,
//            List<MultipartFile> images,
//            UserPrincipal currentUser
//    ) {
//
//        // 1️⃣ Tạo Status
//        Status status = Status.builder()
//                .content(request.getContent())
//                .visibility(request.getVisibility())
//                .user(User.builder().id(currentUser.getId()).build())
//                .active(true)
//                .build();
//
//        statusRepository.save(status);
//
//        List<StatusImage> statusImages = new ArrayList<>();
//        List<String> uploadedPublicIds = new ArrayList<>();
//
//        // 2️⃣ Upload & lưu ảnh
//        if (images != null && !images.isEmpty()) {
//
//            if (images.size() > 10) {
//                throw new IllegalArgumentException("Tối đa 10 ảnh cho mỗi status");
//            }
//
//            try {
//                String folder = "statuses/" + status.getId();
//                int order = 0;
//                for (MultipartFile file : images) {
//
//                    ImageUploadResult upload = imageUploadService.upload(file, folder);
//                    uploadedPublicIds.add(upload.getPublicId());
//
//                    StatusImage image = StatusImage.builder()
//                            .status(status)
//                            .url(upload.getUrl())
//                            .publicId(upload.getPublicId())
//                            .sortOrder(order++)
//                            .build();
//
//                    statusImages.add(image);
//                }
//
//                statusImageRepository.saveAll(statusImages);
//
//            } catch (Exception ex) {
//                // rollback DB + cleanup ảnh đã upload
//                uploadedPublicIds.forEach(imageUploadService::delete);
//                throw ex;
//            }
//        }
//
//        return mapToResponse(status, statusImages);
//    }

    /* =========================
       GET STATUS
       ========================= */

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public StatusResponse getStatusById(Long statusId, UserPrincipal currentUser) {

        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status không tồn tại"));

        if (!status.isActive()) {
            throw new EntityNotFoundException("Status đã bị xoá");
        }

        // TODO: enforce privacy (PUBLIC / FRIENDS_ONLY / ONLY_ME)

        List<StatusImage> images =
                statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());

        return mapToResponse(status, images);
    }

    /* =========================
       DELETE STATUS (SOFT)
       ========================= */

    @Transactional
    @Override
    public void deleteStatus(Long statusId, UserPrincipal currentUser) {

        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status không tồn tại"));

        if (!status.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Không có quyền xoá status này");
        }

        status.setActive(false);
        statusRepository.save(status);
    }

    @Override
    public Page<StatusResponse> getPublicStatusesByUser(Long userId, int page, int size) {
        return null;
    }

    /* =========================
       MAPPER
       ========================= */

    private StatusResponse mapToResponse(Status status, List<StatusImage> images) {

        return StatusResponse.builder()
                .id(status.getId())
                .content(status.getContent())
                .visibility(status.getVisibility().name())
                .createdAt(status.getCreatedAt())
                .imageUrls(
                        images.stream()
                                .map(img -> StatusImageResponse.builder()
                                        .id(img.getId())
                                        .url(img.getUrl())
                                        .sortOrder(img.getSortOrder())
                                        .build())
                                .toList()
                )
                .build();
    }

//    @Override
//    @Transactional
//    public Page<StatusResponse> getPublicStatusesByUser(
//            Long userId,
//            int page,
//            int size
//    ) {
//        log.info("Fetching public statuses for user: {} with page: {}, size: {}", userId, page, size);
//
//        // VALIDATION: Kiểm tra input hợp lệ
//        if (page < 0) {
//            throw new IllegalArgumentException("Page number không thể âm");
//        }
//        if (size <= 0 || size > MAX_PAGE_SIZE) {
//            throw new IllegalArgumentException(
//                    "Size phải từ 1 đến " + MAX_PAGE_SIZE + ", nhận được: " + size
//            );
//        }
//
//        //  CHECK: User có tồn tại không?
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> {
//                    log.warn("User not found with id: {}", userId);
//                    return new EntityNotFoundException("Người dùng không tồn tại với id: " + userId);
//                });
//
//        //  FETCH: Lấy dữ liệu từ DB với pagination
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Status> statusPage = statusRepository.findPublicStatusesByUser(
//                userId,
//                Status.Visibility.PUBLIC,
//                pageable  // Không cast type
//        );
//
//        log.info("Found {} public statuses for user: {}", statusPage.getTotalElements(), userId);
//
//        // MAPPING: Chuyển từ Status entity sang StatusResponse DTO
//        return statusPage.map(status -> mapToResponse(status));
//    }


    // mapper

    private StatusResponse mapStatusResponse(Status status, List<StatusImage> images, Long totalComments, Long totalLikes) {
        return StatusResponse.builder()
                .id(status.getId())
                .content(status.getContent())
                .visibility(status.getVisibility().name())
                .isActive(status.isActive())
                .createdAt(status.getCreatedAt())
                .updatedAt(status.getUpdatedAt())
                .likesCount(totalLikes)
                .commentsCount(totalComments)
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
}
