package com.be_mxh.dto.user;

import com.be_mxh.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserSearchResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;

    public static ChatUserSearchResponse fromEntity(User user) {
        return ChatUserSearchResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
