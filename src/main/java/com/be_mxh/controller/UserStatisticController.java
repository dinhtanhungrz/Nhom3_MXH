package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.statistics.NewUserChartDto;
import com.be_mxh.enums.StatisticType;
import com.be_mxh.service.UserStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class UserStatisticController {
  private final UserStatisticService userStatisticService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/new-users")
  public ResponseEntity<ApiResponse<NewUserChartDto>> getNewUsers(
    @RequestParam(defaultValue = "WEEK") StatisticType type,
    @RequestParam(required = false) Integer month,
    @RequestParam(required = false) Integer year) {

    NewUserChartDto chartData = userStatisticService.getNewUserStatistics(type, month, year);

    return ResponseEntity.ok(ApiResponse.<NewUserChartDto>builder()
      .code(200)
      .message("Get new user statistics successfully")
      .data(chartData)
      .build());
  }
}
