package com.be_mxh.service.impl;

import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.repository.StatusImageRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.ImageUploadService;
import com.be_mxh.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final StatusImageRepository statusImageRepository;
    private final ImageUploadService imageUploadService;
    private final UserRepository userRepository;

    /* =========================
       CREATE STATUS
       ========================= */


    @Override
    public Status createStatus(String content, List<MultipartFile> images, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Status status = Status.builder()
                .content(content)
                .user(user)
                .visibility(Status.Visibility.PUBLIC)
                .active(true)
                .build();

        // Lưu status trước để có ID
        status = statusRepository.save(status);

        // Upload ảnh nếu có
        if (images != null && !images.isEmpty()) {
            String folder = "statuses/" + status.getId();
            int sortOrder = 0;

            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                ImageUploadResult uploadResult =
                        imageUploadService.upload(file, folder);

                StatusImage statusImage = StatusImage.builder()
                        .status(status)
                        .url(uploadResult.getUrl())
                        .publicId(uploadResult.getPublicId())
                        .sortOrder(sortOrder++)
                        .build();

                statusImageRepository.save(statusImage);
            }
        }

        return status;
    }

    @Override
    public List<Status> getFeedStatuses(Long userId) {
        return List.of();
    }

    @Override
    public Status getStatusById(Long statusId, Long userId) {
        return null;
    }

    @Override
    public void deleteStatus(Long statusId, Long userId) {

    }

    @Transactional
    @Override
    public StatusResponse createStatus(
            CreateStatusRequest request,
            List<MultipartFile> images,
            UserPrincipal currentUser
    ) {

        // 1️⃣ Tạo Status
        Status status = Status.builder()
                .content(request.getContent())
                .visibility(request.getVisibility())
                .user(User.builder().id(currentUser.getId()).build())
                .active(true)
                .build();

        statusRepository.save(status);

        List<StatusImage> statusImages = new ArrayList<>();
        List<String> uploadedPublicIds = new ArrayList<>();

        // 2️⃣ Upload & lưu ảnh
        if (images != null && !images.isEmpty()) {

            if (images.size() > 10) {
                throw new IllegalArgumentException("Tối đa 10 ảnh cho mỗi status");
            }

            try {
                String folder = "statuses/" + status.getId();
                int order = 0;
                for (MultipartFile file : images) {

                    ImageUploadResult upload = imageUploadService.upload(file,folder);
                    uploadedPublicIds.add(upload.getPublicId());

                    StatusImage image = StatusImage.builder()
                            .status(status)
                            .url(upload.getUrl())
                            .publicId(upload.getPublicId())
                            .sortOrder(order++)
                            .build();

                    statusImages.add(image);
                }

                statusImageRepository.saveAll(statusImages);

            } catch (Exception ex) {
                // rollback DB + cleanup ảnh đã upload
                uploadedPublicIds.forEach(imageUploadService::delete);
                throw ex;
            }
        }

        return mapToResponse(status, statusImages);
    }

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
                                .map(StatusImage::getUrl)
                                .toList()
                )
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public Page<StatusResponse> getPublicStatusesByUser(
            Long userId,
            int page,
            int size
    ) {
        log.info("Fetching public statuses for user: {} with page: {}, size: {}", userId, page, size);

        // VALIDATION: Kiểm tra input hợp lệ
        if (page < 0) {
            throw new IllegalArgumentException("Page number không thể âm");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Size phải từ 1 đến " + MAX_PAGE_SIZE + ", nhận được: " + size
            );
        }

        //  CHECK: User có tồn tại không?
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new EntityNotFoundException("Người dùng không tồn tại với id: " + userId);
                });

        //  FETCH: Lấy dữ liệu từ DB với pagination
        Pageable pageable = PageRequest.of(page, size);

        Page<Status> statusPage = statusRepository.findPublicStatusesByUser(
                userId,
                Status.Visibility.PUBLIC,
                pageable  // Không cast type
        );

        log.info("Found {} public statuses for user: {}", statusPage.getTotalElements(), userId);

        // MAPPING: Chuyển từ Status entity sang StatusResponse DTO
        return statusPage.map(status -> mapToResponse(status));
    }

    /**
     * Helper method: Map Status entity to StatusResponse DTO
     * Đã tối ưu: Load images chỉ 1 lần
     */
    private StatusResponse mapToResponse(Status status) {
        List<String> imageUrls = statusImageRepository
                .findByStatusIdOrderBySortOrderAsc(status.getId())
                .stream()
                .map(StatusImage::getUrl)
                .toList();

        return StatusResponse.builder()
                .id(status.getId())
                .username(status.getUser().getUsername())
                .content(status.getContent())
                .visibility(String.valueOf(status.getVisibility()))
                .createdAt(status.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
