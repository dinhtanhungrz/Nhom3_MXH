package com.be_mxh.controller;

import com.be_mxh.entity.Status;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.service.StatusService;
import com.be_mxh.service.impl.CommentServiceImpl;
import com.be_mxh.service.impl.LikeServiceImpl;
import com.be_mxh.service.impl.StatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    /**
     * Tạo status mới + upload nhiều ảnh
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStatus(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        if ((content == null || content.trim().isEmpty())
                && (images == null || images.isEmpty())) {
            return ResponseEntity.badRequest()
                    .body("Status phải có nội dung hoặc ảnh");
        }

        Status status = statusService.createStatus(
                content,
                images,
                userPrincipal.getId()
        );

        return ResponseEntity.ok(status);
    }

    /**
     * Lấy danh sách status (news feed)
     */
    @GetMapping
    public ResponseEntity<List<Status>> getStatuses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<Status> statuses =
                statusService.getFeedStatuses(userPrincipal.getId());

        return ResponseEntity.ok(statuses);
    }

    /**
     * Lấy chi tiết 1 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Status> getStatusById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Status status = statusService.getStatusById(id, userPrincipal.getId());
        return ResponseEntity.ok(status);
    }

    /**
     * Xoá status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        statusService.deleteStatus(id, userPrincipal.getId());
        return ResponseEntity.ok("Xoá status thành công");
    }

    @GetMapping("/query")
    public ResponseEntity<?> query(@RequestParam("query") String query) {
        List<Status> statuses = statusService.findAllByContentContaining(query);
        return ResponseEntity.ok(statuses);
    }

//    @GetMapping("/user/{ownerId}")
//    public List<Status> getUserStatuses(
//            @PathVariable Long ownerId,
//            @AuthenticationPrincipal UserPrincipal currentUser
//    ) {
//
//        Long viewerId = currentUser.getId();
//
//        return statusService.getVisibleStatuses(ownerId, viewerId);
//    }
}
