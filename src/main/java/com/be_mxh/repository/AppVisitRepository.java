package com.be_mxh.repository;

import com.be_mxh.entity.AppVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppVisitRepository extends JpaRepository<AppVisit, Long> {

  Optional<AppVisit> findByVisitDate(LocalDate visitDate);

  @Query("SELECT a FROM AppVisit a WHERE a.visitDate BETWEEN :start AND :end ORDER BY a.visitDate ASC")
  List<AppVisit> findByVisitDateBetween(
    @Param("start") LocalDate start,
    @Param("end") LocalDate end
  );
}
