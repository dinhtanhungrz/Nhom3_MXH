package com.be_mxh.dto.status;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class LikeStatus {
    private Long statusId;
    private long likeCount;
    private boolean liked;

}
