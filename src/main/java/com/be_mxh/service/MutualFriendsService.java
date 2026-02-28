package com.be_mxh.service;

import com.be_mxh.dto.user.MutualFriends;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MutualFriendsService {
    Page<MutualFriends> getMutualFriends(Long targetUserId, Pageable pageable);
}
