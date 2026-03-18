package com.be_mxh.service.impl;

import com.be_mxh.dto.statistics.ChartDatasetDto;
import com.be_mxh.dto.statistics.VisitChartDto;
import com.be_mxh.entity.AppVisit;
import com.be_mxh.enums.StatisticType;
import com.be_mxh.repository.AppVisitRepository;
import com.be_mxh.service.AppVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppVisitServiceImpl implements AppVisitService {
  private final AppVisitRepository appVisitRepository;

  @Transactional
  @Override
  public void recordVisit() {
    LocalDate today = LocalDate.now();
    AppVisit visit = appVisitRepository.findByVisitDate(today)
      .orElseGet(() -> AppVisit.builder()
        .visitDate(today)
        .visitCount(0L)
        .build());
    visit.setVisitCount(visit.getVisitCount() + 1);
    appVisitRepository.save(visit);
  }

  @Override
  public VisitChartDto getVisitStatistics(StatisticType type, Integer month, Integer year) {
    return switch (type) {
      case WEEK -> buildWeeklyChart();
      case MONTH -> buildMonthlyChart(month, year);
      case YEAR -> buildYearlyChart(year);
    };
  }

  // ── WEEK: 7 ngày tính từ hôm nay lùi về (kể cả hôm nay) ────────────────
  private VisitChartDto buildWeeklyChart() {
    LocalDate today = LocalDate.now();
    LocalDate startDay = today.minusDays(6); // 7 ngày gồm cả hôm nay

    List<AppVisit> records = appVisitRepository.findByVisitDateBetween(startDay, today);
    Map<LocalDate, Long> visitMap = toMap(records);

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    for (int i = 6; i >= 0; i--) {
      LocalDate day = today.minusDays(i);
      labels.add(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        + " " + day.getDayOfMonth()); // "Mon 12"
      data.add(visitMap.getOrDefault(day, 0L));
    }

    return buildChart("Lượt truy cập trang (7 ngày gần đây)", labels, data);
  }

  // ── MONTH: từng ngày trong tháng chọn ───────────────────────────────────
  private VisitChartDto buildMonthlyChart(Integer month, Integer year) {
    LocalDate today = LocalDate.now();
    int targetMonth = (month != null) ? month : today.getMonthValue();
    int targetYear = (year != null) ? year : today.getYear();

    if (targetMonth < 1 || targetMonth > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    LocalDate firstDay = LocalDate.of(targetYear, targetMonth, 1);
    LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());

    boolean isCurrentMonth = targetYear == today.getYear() && targetMonth == today.getMonthValue();
    if (isCurrentMonth) lastDay = today;

    List<AppVisit> records = appVisitRepository.findByVisitDateBetween(firstDay, lastDay);
    Map<LocalDate, Long> visitMap = toMap(records);

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    LocalDate cursor = firstDay;
    while (!cursor.isAfter(lastDay)) {
      labels.add(String.valueOf(cursor.getDayOfMonth()));
      data.add(visitMap.getOrDefault(cursor, 0L));
      cursor = cursor.plusDays(1);
    }

    String chartLabel = String.format("Lượt truy cập trang (%s %d)",
      Month.of(targetMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH), targetYear);

    return buildChart(chartLabel, labels, data);
  }

  // ── YEAR: từng tháng trong năm chọn ─────────────────────────────────────
  private VisitChartDto buildYearlyChart(Integer year) {
    LocalDate today = LocalDate.now();
    int targetYear = (year != null) ? year : today.getYear();

    boolean isCurrentYear = targetYear == today.getYear();
    int lastMonth = isCurrentYear ? today.getMonthValue() : 12;

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    for (int m = 1; m <= lastMonth; m++) {
      LocalDate firstDay = LocalDate.of(targetYear, m, 1);
      LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());

      if (isCurrentYear && m == today.getMonthValue()) lastDay = today;

      List<AppVisit> records = appVisitRepository.findByVisitDateBetween(firstDay, lastDay);
      long total = records.stream().mapToLong(AppVisit::getVisitCount).sum();

      labels.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
      data.add(total);
    }

    return buildChart("Lượt truy cập trang (" + targetYear + ")", labels, data);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────
  private Map<LocalDate, Long> toMap(List<AppVisit> records) {
    return records.stream()
      .collect(Collectors.toMap(AppVisit::getVisitDate, AppVisit::getVisitCount));
  }

  private VisitChartDto buildChart(String label, List<String> labels, List<Long> data) {
    ChartDatasetDto dataset = ChartDatasetDto.builder()
      .label(label)
      .data(data)
      .borderColor("#7c3aed")
      .backgroundColor("rgba(124,58,237,0.7)")
      .build();

    return VisitChartDto.builder()
      .labels(labels)
      .datasets(List.of(dataset))
      .build();
  }
}
