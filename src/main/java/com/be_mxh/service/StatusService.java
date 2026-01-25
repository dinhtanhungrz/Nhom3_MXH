package com.be_mxh.service;

import com.be_mxh.entity.Status;

import java.util.List;

public interface StatusService {
    Status create(String content, String image, String username);

    List<Status> feed();

    void delete(Long postId, String username);
}
