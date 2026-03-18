package com.be_mxh.service;

import com.be_mxh.dto.statistics.NewUserChartDto;
import com.be_mxh.enums.StatisticType;

public interface UserStatisticService {
  NewUserChartDto getNewUserStatistics(StatisticType type, Integer month, Integer year);

}
