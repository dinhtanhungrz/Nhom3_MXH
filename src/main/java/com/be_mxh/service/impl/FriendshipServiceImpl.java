package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.entity.Friendship;
import com.be_mxh.entity.Notification;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.FriendshipRepository;
import com.be_mxh.repository.NotificationRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.FriendshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class FriendshipServiceImpl implements FriendshipService {
    private final String DEFAULT_RELATIONSHIP = "NONE";
    private final String PENDING_SENT = "PENDING_SENT";
    private final String PENDING_RECEIVED = "PENDING_RECEIVED";
    private final String FRIENDS = "FRIENDS";
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public String getRelationship(Long currentUserId, Long targetUserId) {
        Friendship friendship = friendshipRepository.findRelationship(currentUserId, targetUserId).orElse(null);

        if (friendship == null) return DEFAULT_RELATIONSHIP;

        if (friendship.getStatus().equals(Friendship.Status.PENDING) && friendship.getRequester().getId().equals(currentUserId))
            return PENDING_SENT;

        if (friendship.getStatus().equals(Friendship.Status.PENDING) && friendship.getRequester().getId().equals(targetUserId))
            return PENDING_RECEIVED;

        if (friendship.getStatus().equals(Friendship.Status.ACCEPTED)) return FRIENDS;

        return DEFAULT_RELATIONSHIP;
    }

    @Override
    @Transactional
    public void friendRequest(Long userAddressesId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        if (currentUserId.equals(userAddressesId)) {
            throw new BadRequestException("Unable to send a friend request to myself");
        }

        User requester = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User addressee = userRepository.findById(userAddressesId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Friendship> existing = friendshipRepository.findRelationship(currentUserId, userAddressesId);
        if (existing.isPresent()) {
            Friendship f = existing.get();

            if (f.getStatus() == Friendship.Status.PENDING &&
                    f.getRequester().getId().equals(userAddressesId)) {

                f.setStatus(Friendship.Status.ACCEPTED);
                friendshipRepository.save(f);
                return;
            }

            throw new BadRequestException("Friend request already exists or users are already friends");
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(Friendship.Status.PENDING);
        friendshipRepository.save(friendship);

        Notification notification = new Notification();
        notification.setActor(requester);
        notification.setReceiver(addressee);
        notification.setType(Notification.NotificationType.FRIEND_REQUEST);
        notification.setEntityType(Notification.EntityType.USER);
        notification.setEntityId(requester.getId());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long userAddressesId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        if (currentUserId.equals(userAddressesId)) {
            throw new BadRequestException("Unable to cancel friend request to myself");
        }

        Friendship friendship = friendshipRepository.findRelationship(currentUserId, userAddressesId)
                .orElseThrow(() -> new BadRequestException("Friendship is not found"));

        if (friendship.getStatus().equals(Friendship.Status.PENDING)) {
            friendshipRepository.delete(friendship);
            return;
        }

        throw new BadRequestException("Friendship is not pending");
    }

    @Override
    @Transactional
    public void unfriend(Long userAddressesId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId.equals(userAddressesId)) {
            throw new BadRequestException("Unable to cancel friend request to myself");
        }

        Friendship friendship = friendshipRepository.findRelationship(currentUserId, userAddressesId)
                .orElseThrow(() -> new BadRequestException("The two users are not friends."));

        if (friendship.getStatus().equals(Friendship.Status.ACCEPTED)) {
            friendshipRepository.delete(friendship);
            return;
        }

        throw new BadRequestException("The two users are not friends");
    }
}
