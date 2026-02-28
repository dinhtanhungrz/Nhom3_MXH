package com.be_mxh.dto.status;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Status Response DTO")
public class StatusResponse {

    @Schema(description = "Status ID", example = "1")
    private Long id;

    @Schema(description = "Username người đăng", example = "john_doe")
    private String username;  // Added

    @Schema(description = "Nội dung status", example = "Hôm nay là ngày đẹp trời")
    private String content;

    @Schema(description = "Chế độ hiển thị", example = "PUBLIC")
    private String visibility;

    @Schema(description = "Thời gian tạo")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Danh sách URL ảnh")
    private List<String> imageUrls;
}