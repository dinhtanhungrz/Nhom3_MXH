package com.be_mxh.repository;

import com.be_mxh.entity.StatusImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusImageRepository extends JpaRepository<StatusImage, Long> {
    List<StatusImage> findByStatusIdOrderBySortOrderAsc(Long statusId);
}
