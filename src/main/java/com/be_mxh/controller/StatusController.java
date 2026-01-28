package com.be_mxh.controller;

import com.be_mxh.entity.Status;
import com.be_mxh.service.impl.CommentServiceImpl;
import com.be_mxh.service.impl.LikeServiceImpl;
import com.be_mxh.service.impl.StatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class StatusController {

    private final StatusServiceImpl postService;
    private final CommentServiceImpl commentService;
    private final LikeServiceImpl likeServiceImpl;

    @PostMapping
    public Status create(@RequestBody Map<String,String> req,
                       Principal p) {
        return postService.create(
                req.get("content"),
                req.get("imageUrl"),
                p.getName()
        );
    }

    @GetMapping
    public List<Status> feed() {
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
        return likeServiceImpl.toggle(id, p.getName());
    }
}
