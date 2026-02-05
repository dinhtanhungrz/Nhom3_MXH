package com.be_mxh.repository;

import java.time.LocalDateTime;

public interface StatusFeedProjection {

    Long getId();
    String getContent();

    Long getUserId();
    String getUsername();

    LocalDateTime getCreatedAt();

    long getLikeCount();
    long getCommentCount();
}
