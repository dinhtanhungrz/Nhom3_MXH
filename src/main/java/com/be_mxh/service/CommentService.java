package com.be_mxh.service;

import com.be_mxh.entity.Comment;
import com.be_mxh.entity.Post;
import com.be_mxh.entity.User;
import com.be_mxh.repository.CommentRepository;
import com.be_mxh.repository.PostRepository;
import com.be_mxh.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    public void comment(Long postId, String content, String username) {

        Post post = postRepo.findById(postId)
                .filter(p -> !p.isDeleted())
                .orElseThrow();

        User user = userRepo
                .findByUsernameOrEmail(username, username)
                .orElseThrow();

        Comment c = new Comment();
        c.setContent(content);
        c.setUser(user);
        c.setPost(post);

        commentRepo.save(c);
        post.setCommentCount(post.getCommentCount() + 1);
    }
}
