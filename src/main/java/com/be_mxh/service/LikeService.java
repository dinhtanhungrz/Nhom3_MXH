package com.be_mxh.service;

import com.be_mxh.dto.status.LikeStatus;

public interface LikeService {
    LikeStatus likeStatus(Long statusId, Long userId);
    LikeStatus unlikeStatus(Long statusId,Long userId);
    LikeStatus getLikeStatus(Long statusId,Long userId);
}
