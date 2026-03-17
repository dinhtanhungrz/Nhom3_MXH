package com.be_mxh.service.impl;

import com.be_mxh.dto.statistics.ChartDatasetDto;
import com.be_mxh.dto.statistics.NewUserChartDto;
import com.be_mxh.enums.StatisticType;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.UserStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserStatisticServiceImpl implements UserStatisticService {
  private final UserRepository userRepository;

  @Override
  public NewUserChartDto getNewUserStatistics(StatisticType type, Integer month, Integer year) {
    return switch (type) {
      case WEEK -> buildWeeklyChart();
      case MONTH -> buildMonthlyChart(month, year);
      case YEAR -> buildYearlyChart(year);
    };
  }

  // ── WEEK: 7 ngày gần nhất (Mon → Sun hoặc tính từ hôm nay lùi 6 ngày) ──
  private NewUserChartDto buildWeeklyChart() {
    LocalDate today = LocalDate.now();
    // Lấy từ đầu tuần (Monday) đến hôm nay
    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    for (int i = 0; i < 7; i++) {
      LocalDate day = startOfWeek.plusDays(i);
      LocalDateTime start = day.atStartOfDay();
      LocalDateTime end = day.atTime(LocalTime.MAX);

      labels.add(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)); // Mon, Tue...
      data.add(userRepository.countByCreatedAtBetween(start, end));
    }

    return buildChart("New Users (This Week)", labels, data);
  }

  // ── MONTH: thống kê theo từng ngày trong tháng được chọn ──
  // Nếu không truyền month/year → mặc định tháng/năm hiện tại
  // Nếu là tháng hiện tại → chỉ lấy đến ngày hôm nay
  private NewUserChartDto buildMonthlyChart(Integer month, Integer year) {
    LocalDate today = LocalDate.now();

    int targetMonth = (month != null) ? month : today.getMonthValue();
    int targetYear = (year != null) ? year : today.getYear();

    // Validate tháng hợp lệ
    if (targetMonth < 1 || targetMonth > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    LocalDate firstDay = LocalDate.of(targetYear, targetMonth, 1);
    LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());

    // Nếu là tháng hiện tại của năm hiện tại → chỉ lấy đến hôm nay
    boolean isCurrentMonth = (targetYear == today.getYear() && targetMonth == today.getMonthValue());
    if (isCurrentMonth) {
      lastDay = today;
    }

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    // Duyệt từng ngày trong tháng
    LocalDate cursor = firstDay;
    while (!cursor.isAfter(lastDay)) {
      LocalDateTime start = cursor.atStartOfDay();
      LocalDateTime end = cursor.atTime(LocalTime.MAX);

      labels.add(String.valueOf(cursor.getDayOfMonth())); // "1", "2", ... "31"
      data.add(userRepository.countByCreatedAtBetween(start, end));

      cursor = cursor.plusDays(1);
    }

    String chartLabel = String.format("New Users (%s %d)",
      Month.of(targetMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
      targetYear);

    return buildChart(chartLabel, labels, data);
  }

  // ── YEAR: thống kê theo từng tháng trong năm được chọn ──
  // Nếu không truyền year → mặc định năm hiện tại
  // Nếu là năm hiện tại → chỉ lấy đến tháng hiện tại
  private NewUserChartDto buildYearlyChart(Integer year) {
    LocalDate today = LocalDate.now();

    int targetYear = (year != null) ? year : today.getYear();

    // Nếu là năm hiện tại → chỉ lấy đến tháng hiện tại, ngược lại lấy đủ 12 tháng
    boolean isCurrentYear = (targetYear == today.getYear());
    int lastMonth = isCurrentYear ? today.getMonthValue() : 12;

    List<String> labels = new ArrayList<>();
    List<Long> data = new ArrayList<>();

    for (int m = 1; m <= lastMonth; m++) {
      LocalDateTime start = LocalDateTime.of(targetYear, m, 1, 0, 0, 0);
      LocalDateTime end = start.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

      // Nếu là tháng hiện tại của năm hiện tại → cắt đến hôm nay
      if (isCurrentYear && m == today.getMonthValue()) {
        end = today.atTime(LocalTime.MAX);
      }

      labels.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)); // "Jan", "Feb"...
      data.add(userRepository.countByCreatedAtBetween(start, end));
    }

    return buildChart("New Users (" + targetYear + ")", labels, data);
  }

  // ── Helper: tạo NewUserChartDto với 1 dataset ──
  private NewUserChartDto buildChart(String label, List<String> labels, List<Long> data) {
    ChartDatasetDto dataset = ChartDatasetDto.builder()
      .label(label)
      .data(data)
      .borderColor("#4F86C6")
      .backgroundColor("rgba(79, 134, 198, 0.1)")
      .fill(false)
      .build();

    return NewUserChartDto.builder()
      .labels(labels)
      .datasets(List.of(dataset))
      .build();
  }
}
