package com.be_mxh.service.impl;

import com.be_mxh.dto.status.LikeStatus;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusLike;
import com.be_mxh.entity.User;
import com.be_mxh.repository.LikeRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.LikeService;
import com.be_mxh.service.NotificationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepo;
    private final StatusRepository statusRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @Override
    public LikeStatus likeStatus(Long statusId, Long userId) {

        Status status = statusRepo.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status không tồn tại"));

        if (!status.isActive()) {
            throw new RuntimeException("Status đã bị xoá");
        }

        boolean alreadyLiked =
                likeRepo.existsByStatusIdAndUserId(statusId, userId);

        if (!alreadyLiked) {

            StatusLike like = new StatusLike();

            like.setStatus(status);

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));

            like.setUser(user);

            likeRepo.save(like);
            
            notificationService.createNotification(status.getUser().getId(), userId, "LIKE_STATUS", "POST", statusId);
        }

        long likeCount = likeRepo.countByStatusId(statusId);

        return new LikeStatus(statusId, likeCount, true);
    }

    @Override
    public LikeStatus unlikeStatus(Long statusId, Long userId) {

        boolean liked =
                likeRepo.existsByStatusIdAndUserId(statusId, userId);

        if (liked) {
            likeRepo.deleteByStatusIdAndUserId(statusId, userId);
            statusRepo.findById(statusId).ifPresent(status -> {
                notificationService.revokeNotification(status.getUser().getId(), userId, "LIKE_STATUS", "POST", statusId);
            });
        }

        long likeCount = likeRepo.countByStatusId(statusId);

        return new LikeStatus(statusId, likeCount, false);
    }

    @Override
    public LikeStatus getLikeStatus(Long statusId, Long userId) {

        long likeCount = likeRepo.countByStatusId(statusId);

        boolean liked =
                likeRepo.existsByStatusIdAndUserId(statusId, userId);

        return new LikeStatus(statusId, likeCount, liked);
    }
}

//    public int toggle(Long postId, String username) {
//
//        Status post = postRepo.findById(postId)
//                .filter(Status::getActive)
//                .orElseThrow();
//
//        User user = userRepo
//                .findByUsernameOrEmail(username, username)
//                .orElseThrow();
//
//        return likeRepo.findByPostIdAndUserId(postId, user.getId())
//                .map(like -> {
//                    likeRepo.delete(like);
//                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
//                    return post.getLikeCount();
//                })
//                .orElseGet(() -> {
//                    Like l = new Like();
//                    l.setPost(post);
//                    l.setUser(user);
//                    likeRepo.save(l);
//                    post.setLikeCount(post.getLikeCount() + 1);
//                    return post.getLikeCount();
//                });
//    }
