package com.be_mxh.controller.user;

import com.be_mxh.entity.Post;
import com.be_mxh.service.CommentService;
import com.be_mxh.service.LikeService;
import com.be_mxh.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;

    @PostMapping
    public Post create(@RequestBody Map<String,String> req,
                       Principal p) {
        return postService.create(
                req.get("content"),
                req.get("imageUrl"),
                p.getName()
        );
    }

    @GetMapping
    public List<Post> feed() {
        return postService.feed();
    }

    @PostMapping("/{id}/comments")
    public void comment(@PathVariable Long id,
                        @RequestBody Map<String,String> req,
                        Principal p) {
        commentService.comment(id, req.get("content"), p.getName());
    }

    @PostMapping("/{id}/like")
    public int like(@PathVariable Long id, Principal p) {
        return likeService.toggle(id, p.getName());
    }
}
