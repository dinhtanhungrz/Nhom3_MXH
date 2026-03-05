package com.be_mxh.service.impl;

import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.dto.status.StatusResponseDisplay;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.repository.*;
import org.springframework.security.access.AccessDeniedException;
import com.be_mxh.service.ImageUploadService;
import com.be_mxh.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final FriendshipRepository friendshipRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    /*
     * =========================
     * CREATE STATUS
     * =========================
     */

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
                if (file.isEmpty())
                    continue;

                ImageUploadResult uploadResult = imageUploadService.upload(file, folder);

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
    public List<StatusResponseDisplay> getFeedStatuses(Long userId) {
        List<Status> results = statusRepository.getNewsfeedStatuses(userId);
        List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
        for(Status status : results){
            List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
            Integer totalLikes = likeRepository.countByStatusId(status.getId());
            Integer totalComments = commentRepository.countByStatusId(status.getId());
            statusResponseDisplays.add(mapStatusResponseDisplay(status, images, totalComments, totalLikes));
        }
        return statusResponseDisplays;
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
            UserPrincipal currentUser) {

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

                    ImageUploadResult upload = imageUploadService.upload(file, folder);
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

    /*
     * =========================
     * GET STATUS
     * =========================
     */

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public StatusResponse getStatusById(Long statusId, UserPrincipal currentUser) {

        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status không tồn tại"));

        if (!status.isActive()) {
            throw new EntityNotFoundException("Status đã bị xoá");
        }

        // TODO: enforce privacy (PUBLIC / FRIENDS_ONLY / ONLY_ME)

        Long ownerId = status.getUser().getId(); // Ai là chủ status?
        Long viewerId = currentUser.getId(); // Ai đang xem?
        switch (status.getVisibility()) {
            case ONLY_ME -> {
                // Chỉ chủ tài khoản mới được xem
                // Nếu người xem KHÁC chủ → ném lỗi 403
                if (!ownerId.equals(viewerId))
                    throw new AccessDeniedException("Bạn không có quyền xem status này");
            }
            case FRIENDS_ONLY -> {
                if (!ownerId.equals(viewerId)) { // chủ thì cứ cho xem
                    // Hỏi database: viewerId và ownerId có phải bạn bè không?
                    boolean areFriends = friendshipRepository
                            .existsAcceptedFriendship(viewerId, ownerId);
                    if (!areFriends)
                        throw new AccessDeniedException("Bạn không có quyền xem status này");
                }
            }
            case PUBLIC -> {
                // Không làm gì — ai cũng xem được
            }
        }

        List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());

        return mapToResponse(status, images);
    }

    /*
     * =========================
     * DELETE STATUS (SOFT)
     * =========================
     */

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

    /*
     * =========================
     * MAPPER
     * =========================
     */

    private StatusResponse mapToResponse(Status status, List<StatusImage> images) {

        return StatusResponse.builder()
                .id(status.getId())
                .content(status.getContent())
                .visibility(status.getVisibility().name())
                .createdAt(status.getCreatedAt())
                .imageUrls(
                        images.stream()
                                .map(StatusImage::getUrl)
                                .toList())
                .build();
    }

    @Override
    public List<StatusResponseDisplay> getVisibleStatuses(Long ownerId, Long viewerId) {
        List<Status> results = statusRepository.findVisibleStatuses(ownerId, viewerId);
        List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
        for(Status status : results){
            List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
            Integer totalLikes = likeRepository.countByStatusId(status.getId());
            Integer totalComments = commentRepository.countByStatusId(status.getId());
            statusResponseDisplays.add(mapStatusResponseDisplay(status, images, totalComments, totalLikes));
        }
        return statusResponseDisplays;
    }

    @Override
    public List<StatusResponseDisplay> searchUserStatuses(Long ownerId, Long viewerId, String keyword) {
        List<Status> results = statusRepository.searchVisibleStatuses(ownerId, viewerId, keyword);
        List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
        for(Status status : results){
            List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
            Integer totalLikes = likeRepository.countByStatusId(status.getId());
            Integer totalComments = commentRepository.countByStatusId(status.getId());
            statusResponseDisplays.add(mapStatusResponseDisplay(status, images, totalComments, totalLikes));
        }
        return statusResponseDisplays;
    }

    @Override
    public List<StatusResponseDisplay> findAllByContentContaining(String query, Long viewerId) {
        List<Status> results = statusRepository.globalSearch(viewerId, query);
        List<StatusResponseDisplay> statusResponseDisplays = new ArrayList<>();
        for(Status status : results){
            List<StatusImage> images = statusImageRepository.findByStatusIdOrderBySortOrderAsc(status.getId());
            Integer totalLikes = likeRepository.countByStatusId(status.getId());
            Integer totalComments = commentRepository.countByStatusId(status.getId());
            statusResponseDisplays.add(mapStatusResponseDisplay(status, images, totalComments, totalLikes));
        }
        return statusResponseDisplays;
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

    private StatusResponseDisplay mapStatusResponseDisplay(Status status, List<StatusImage> images, Integer totalComments, Integer totalLikes) {
        return StatusResponseDisplay.builder()
                .id(status.getId())
                .content(status.getContent())
                .visibility(status.getVisibility().name())
                .isActive(status.isActive())
                .createdAt(status.getCreatedAt())
                .updatedAt(status.getUpdatedAt())
                .likesCount(totalLikes)
                .commentsCount(totalComments)
                .authorId(status.getUser().getId())
                .authorName(status.getUser().getFullName())
                .authorAvatarUrl(status.getUser().getAvatarUrl())
                .imageUrls(images.stream().map(this::mapToImageUrl).toList())
                .build();
    }

    private String mapToImageUrl(StatusImage image){
        return image.getUrl();
    }
}
