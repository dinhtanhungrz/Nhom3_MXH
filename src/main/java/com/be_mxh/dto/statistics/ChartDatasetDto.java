package com.be_mxh.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDatasetDto {
  private String label;
  private List<Long> data;

  // Chart.js optional styling (frontend có thể dùng hoặc bỏ qua)
  private String borderColor;
  private String backgroundColor;
  private boolean fill = false;
  private boolean tension = false;
}
