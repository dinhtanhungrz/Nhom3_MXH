package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.user.MutualFriends;
import com.be_mxh.entity.User;
import com.be_mxh.repository.MutualFriendsRepository;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.MutualFriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MutualFriendsServiceImpl implements MutualFriendsService {
    private final MutualFriendsRepository mutualFriendsRepository;
    private final SecurityUtils securityUtils;

    @Override
    public Page<MutualFriends> getMutualFriends(Long targetUserId, Pageable pageable) {

        Long currentUserId = securityUtils.getCurrentUserId();

        return mutualFriendsRepository
                .findMutualFriends(currentUserId, targetUserId, pageable)
                .map(this::mapToDto);
    }

    private MutualFriends mapToDto(User user) {
        MutualFriends dto = new MutualFriends();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setFullName(user.getFullName());
        return dto;
    }
}