package com.be_mxh.service;

import com.be_mxh.dto.user.FriendshipsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {
    String getRelationship(Long currentUserId, Long targetUserId);

    void friendRequest(Long userAddressesId);

    void cancelFriendRequest(Long userAddressesId);

    void unfriend(Long userAddressesId);

    Page<FriendshipsResponse> getFriends(Long userId, Pageable pageable);
}
