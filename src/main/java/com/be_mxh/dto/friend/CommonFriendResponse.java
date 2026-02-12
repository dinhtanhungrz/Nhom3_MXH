package com.be_mxh.dto.friend;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonFriendResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatar;
    private int mutualCount; // Số bạn chung
}