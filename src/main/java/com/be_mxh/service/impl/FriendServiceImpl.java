package com.be_mxh.service.impl;

import com.be_mxh.dto.friend.CommonFriendResponse;
import com.be_mxh.entity.User;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.FriendshipRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CommonFriendResponse> getCommonFriends(Long targetUserId, int page, int size) {
        User currentUser = getCurrentUser();

        // 1. Lấy Page<User> từ Repository
        Page<User> userPage = friendshipRepository.findCommonFriends(
                currentUser.getId(),
                targetUserId,
                PageRequest.of(page, size)
        );

        // 2. Chuyển đổi (Map) Page<User> sang Page<CommonFriendResponse>
        return userPage.map(user -> CommonFriendResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .avatar(user.getAvatarUrl())
                .mutualCount(0)
                .build());
    }

    @Override
    public Page<CommonFriendResponse> getFriendsOfUser(Long targetUserId, int page, int size) {
// 1. Tìm người dùng mục tiêu
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 2. Kiểm tra quyền riêng tư (Feature 13)
        if (targetUser.getDisplayFriendsStatus() == User.DisplayFriendsStatus.PRIVATE) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra xem có phải khách (chưa đăng nhập) không
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                throw new RuntimeException("Bạn cần đăng nhập để xem danh sách này.");
            }

            User currentUser = getCurrentUser();
            // Nếu không phải chủ sở hữu, chặn truy cập
            if (!targetUser.getId().equals(currentUser.getId())) {
                throw new RuntimeException("Danh sách bạn bè của người dùng này là riêng tư.");
            }
        }

        // 3. Lấy dữ liệu từ Repository
        Page<User> friendsPage = friendshipRepository.findAllFriendsByUserId(
                targetUserId,
                PageRequest.of(page, size)
        );

        return friendsPage.map(user -> CommonFriendResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .avatar(user.getAvatarUrl())
                .build());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = auth.getPrincipal();
        String username = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

}