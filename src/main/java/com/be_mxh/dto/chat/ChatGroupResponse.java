package com.be_mxh.dto.chat;

import com.be_mxh.entity.ChatGroup;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO cho Chat Group
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupResponse {
    private Long id;
    private String groupName;
    private String description;
    private Long creatorId;
    private String creatorUsername;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatGroupMemberResponse> members;
    private Integer memberCount;

    public static ChatGroupResponse fromEntity(ChatGroup group) {
        List<ChatGroupMemberResponse> memberResponses = group.getMembers().stream()
                .map(ChatGroupMemberResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ChatGroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .creatorId(group.getCreator().getId())
                .creatorUsername(group.getCreator().getUsername())
                .avatarUrl(group.getAvatarUrl())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .members(memberResponses)
                .memberCount(group.getMembers().size())
                .build();
    }
}
