package com.be_mxh.service;

import com.be_mxh.dto.user.FriendshipsResponse;
import com.be_mxh.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FriendshipService {
    String getRelationship(Long currentUserId, Long targetUserId);

    void friendRequest(Long userAddressesId);

    void cancelFriendRequest(Long userAddressesId);

    void unfriend(Long userAddressesId);

    Page<FriendshipsResponse> getFriends(Long userId, Pageable pageable);

    List<FriendshipsResponse> getPendingRequests(Long id);

    void acceptRequest(Long requesterId);

    void rejectRequest(Long requesterId);

    List<FriendshipsResponse> getSuggestions(Long userId);
}
