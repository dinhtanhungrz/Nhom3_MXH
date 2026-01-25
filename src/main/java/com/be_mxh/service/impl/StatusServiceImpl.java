package com.be_mxh.service.impl;

import com.be_mxh.entity.Status;
import com.be_mxh.entity.User;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Status create(String content, String image, String username) {

        User user = userRepository
                .findByUsernameOrEmail(username, username)
                .orElseThrow();

        Status post = new Status();
        post.setContent(content);
//        post.setImageUrl(image);
        post.setUser(user);

        return postRepository.save(post);
    }

    @Override
    public List<Status> feed() {
        return postRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public void delete(Long postId, String username) {
        Status post = postRepository.findById(postId).orElseThrow();
        if (!post.getUser().getUsername().equals(username))
            throw new RuntimeException("Forbidden");
        post.setActive(true);
    }
}
