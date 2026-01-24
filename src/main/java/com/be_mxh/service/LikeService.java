package com.be_mxh.service;

import com.be_mxh.entity.Post;
import com.be_mxh.entity.PostLike;
import com.be_mxh.entity.User;
import com.be_mxh.repository.PostLikeRepository;
import com.be_mxh.repository.PostRepository;
import com.be_mxh.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final PostLikeRepository likeRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    public int toggle(Long postId, String username) {

        Post post = postRepo.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow();

        User user = userRepo
                .findByUsernameOrEmail(username, username)
                .orElseThrow();

        return likeRepo.findByPostIdAndUserId(postId, user.getId())
                .map(like -> {
                    likeRepo.delete(like);
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                    return post.getLikeCount();
                })
                .orElseGet(() -> {
                    PostLike l = new PostLike();
                    l.setPost(post);
                    l.setUser(user);
                    likeRepo.save(l);
                    post.setLikeCount(post.getLikeCount() + 1);
                    return post.getLikeCount();
                });
    }
}
