package com.be_mxh.dto.chat;

import com.be_mxh.entity.ChatGroupMember;
import lombok.*;

/**
 * Response DTO cho Chat Group Member
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String joinedAt;

    public static ChatGroupMemberResponse fromEntity(ChatGroupMember member) {
        return ChatGroupMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .fullName(member.getUser().getFullName())
                .avatarUrl(member.getUser().getAvatarUrl())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt().toString())
                .build();
    }
}
