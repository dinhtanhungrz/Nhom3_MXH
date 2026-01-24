package com.be_mxh.service;

import com.be_mxh.entity.Post;
import com.be_mxh.entity.User;
import com.be_mxh.repository.PostRepository;
import com.be_mxh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;

    public Post create(String content, String image, String username) {

        User user = userRepo
                .findByUsernameOrEmail(username, username)
                .orElseThrow();

        Post post = new Post();
        post.setContent(content);
        post.setImageUrl(image);
        post.setUser(user);

        return postRepo.save(post);
    }

    public List<Post> feed() {
        return postRepo.findByDeletedFalseOrderByCreatedAtDesc();
    }

    public void delete(Long postId, String username) {
        Post post = postRepo.findById(postId).orElseThrow();
        if (!post.getUser().getUsername().equals(username))
            throw new RuntimeException("Forbidden");
        post.setDeleted(true);
    }
}
