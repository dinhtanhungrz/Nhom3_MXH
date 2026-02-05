package com.be_mxh.service.impl;

import com.be_mxh.repository.LikeRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.LikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepo;
    private final StatusRepository postRepo;
    private final UserRepository userRepo;

    @Override
    public int toggle(Long postId, String username) {
        return 0;
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
}
