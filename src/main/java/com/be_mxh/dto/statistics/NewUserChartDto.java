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
public class NewUserChartDto {
  private List<String> labels;          // Trục X: ["Mon", "Tue",...] / ["Jan",...] / ["2020",...]
  private List<ChartDatasetDto> datasets; // Trục Y: danh sách dataset
}
