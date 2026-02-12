package com.be_mxh.service;

import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StatusService {

    /**
     * Tạo status mới (có thể kèm nhiều ảnh)
     */
    Status createStatus(
            String content,
            List<MultipartFile> images,
            Long userId
    );

    /**
     * Lấy danh sách status cho news feed
     */
    List<Status> getFeedStatuses(Long userId);

    /**
     * Lấy chi tiết 1 status
     */
    Status getStatusById(Long statusId, Long userId);

    /**
     * Xoá status (chỉ chủ status được xoá)
     */


    void deleteStatus(Long statusId, Long userId);


    @Transactional
    StatusResponse createStatus(
            CreateStatusRequest request,
            List<MultipartFile> images,
            UserPrincipal currentUser
    );

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    StatusResponse getStatusById(Long statusId, UserPrincipal currentUser);

    @Transactional
    void deleteStatus(Long statusId, UserPrincipal currentUser);

    List<Status> findAllByContentContaining(String query);


//    List<Status> getVisibleStatuses(Long ownerId, Long viewerId);
}
