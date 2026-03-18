package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.statistics.VisitChartDto;
import com.be_mxh.enums.StatisticType;
import com.be_mxh.service.AppVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app-visits")
@RequiredArgsConstructor
public class AppVisitController {

  private final AppVisitService appVisitService;

  // ── FE gọi khi user load/refresh trang ──────────────────────────────────
  @PostMapping("/record")
  public ResponseEntity<ApiResponse<Void>> recordVisit() {
    appVisitService.recordVisit();
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(200)
      .message("Visit recorded successfully")
      .build());
  }

  // ── Admin thống kê ───────────────────────────────────────────────────────
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/statistics")
  public ResponseEntity<ApiResponse<VisitChartDto>> getVisitStats(
    @RequestParam(defaultValue = "WEEK") StatisticType type,
    @RequestParam(required = false) Integer month,
    @RequestParam(required = false) Integer year) {

    VisitChartDto chartData = appVisitService.getVisitStatistics(type, month, year);

    return ResponseEntity.ok(ApiResponse.<VisitChartDto>builder()
      .code(200)
      .message("Get visit statistics successfully")
      .data(chartData)
      .build());
  }
}
