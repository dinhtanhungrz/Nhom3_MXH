package com.be_mxh.service;

import com.be_mxh.dto.status.CreateStatusRequest;
import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StatusService {

    void createStatus(String content, String visibility, List<MultipartFile> images);

    List<StatusResponse> getStatusesByProfile();

    List<StatusResponse> getFeedStatuses(Long userId);

    /**
     * Lấy chi tiết 1 status
     */
    Status getStatusById(Long id, Long userId);

    /**
     * Xoá status (chỉ chủ status được xoá)
     */
    void deleteStatus(Long id, Long userId);

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    StatusResponse getStatusById(Long statusId, UserPrincipal currentUser);

    @Transactional
    void deleteStatus(Long statusId, UserPrincipal currentUser);

    Page<StatusResponse> getPublicStatusesByUser(
            Long userId,
            int page,
            int size
    );

}
