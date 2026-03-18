package com.be_mxh.dto.statistics;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitChartDto {
  private List<String> labels;
  private List<ChartDatasetDto> datasets;
}
