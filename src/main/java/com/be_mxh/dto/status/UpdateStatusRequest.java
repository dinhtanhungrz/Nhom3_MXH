package com.be_mxh.dto.status;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {
  private String content;                  // null = không đổi caption
  private String visibility;               // null = không đổi visibility
  private List<Long> deleteImageIds;       // danh sách id ảnh cũ cần xóa
}
