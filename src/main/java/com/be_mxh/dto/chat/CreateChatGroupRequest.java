package com.be_mxh.dto.chat;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO để tạo group chat mới
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatGroupRequest {
    /**
     * Tên nhóm chat (bắt buộc)
     */
    private String groupName;

    /**
     * Mô tả nhóm chat (tùy chọn)
     */
    private String description;

    /**
     * Danh sách ID những người dùng cần thêm vào nhóm (bắt buộc)
     */
    private List<Long> memberIds;
}
