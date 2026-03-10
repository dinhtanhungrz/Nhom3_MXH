package com.be_mxh.dto.status;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class StatusImageResponse {
    private Long id;
    private String url;
    private Integer sortOrder;
}
