package com.be_mxh.service.impl;

import com.be_mxh.entity.Comment;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.User;
import com.be_mxh.repository.CommentRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.CommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;

    @Override
    public void comment(Long postId, String content, String username) {

        Status post = statusRepository.findById(postId)
                .filter(p -> p.getActive())
                .orElseThrow();

        User user = userRepository
                .findByUsernameOrEmail(username, username)
                .orElseThrow();

        Comment c = new Comment();
        c.setContent(content);
        c.setUser(user);
        c.setStatus(post);

        commentRepository.save(c);
//        post.setCommentCount(post.getCommentCount() + 1);
    }
}
