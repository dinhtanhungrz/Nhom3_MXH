package com.be_mxh.dto.status;

import com.be_mxh.dto.image.ImageUploadResult;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusResponse {
    private Long id;
    private String content;
    private String visibility;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
}