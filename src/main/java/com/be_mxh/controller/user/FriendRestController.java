package com.be_mxh.controller.user;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.friend.CommonFriendResponse;
import com.be_mxh.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FriendRestController {

    private final FriendService friendService;

    @GetMapping("/common/{targetId}")
    public ResponseEntity<?> getCommonFriends(
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Lấy dữ liệu phân trang từ service
        Page<CommonFriendResponse> commonFriends = friendService.getCommonFriends(targetId, page, size);

        // Đóng gói data theo cấu trúc phân trang cho Infinite Scroll
        Map<String, Object> paginationData = Map.of(
                "content", commonFriends.getContent(),
                "totalPages", commonFriends.getTotalPages(),
                "totalElements", commonFriends.getTotalElements(),
                "last", commonFriends.isLast(),
                "pageNumber", commonFriends.getNumber()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Map<String, Object>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get common friends successfully")
                        .data(paginationData)
                        .build()
        );
    }
}