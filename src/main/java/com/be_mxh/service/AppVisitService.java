package com.be_mxh.service;

import com.be_mxh.dto.statistics.VisitChartDto;
import com.be_mxh.enums.StatisticType;

public interface AppVisitService {

  void recordVisit();

  VisitChartDto getVisitStatistics(StatisticType type, Integer month, Integer year);
}
