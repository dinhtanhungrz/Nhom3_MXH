package com.be_mxh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_visits")
public class AppVisit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Ngày xảy ra lượt truy cập (không lưu giờ để group by ngày dễ hơn)
  @Column(nullable = false)
  private LocalDate visitDate;

  // Tổng lượt trong ngày đó — increment mỗi khi có request
  @Column(nullable = false)
  private Long visitCount;
}
