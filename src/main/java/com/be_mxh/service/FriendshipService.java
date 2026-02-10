package com.be_mxh.service;

public interface FriendshipService {
    String getRelationship(Long currentUserId, Long targetUserId);

    void friendRequest(Long userAddressesId);

    void cancelFriendRequest(Long userAddressesId);
}
