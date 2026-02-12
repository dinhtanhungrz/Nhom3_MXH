package com.be_mxh.service;

import com.be_mxh.dto.friend.CommonFriendResponse;
import com.be_mxh.entity.User;
import org.springframework.data.domain.Page;

public interface FriendService {
    Page<CommonFriendResponse> getCommonFriends(Long targetUserId, int page, int size);
    Page<CommonFriendResponse> getFriendsOfUser(Long targetUserId, int page, int size);
}
