package com.be_mxh.repository;

import com.be_mxh.entity.StatusLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<StatusLike, Long> {
// ham dem luot like
    long countByStatusId(Long statusId);
    //kiem tra like chua
    boolean existsByStatusIdAndUserId(Long statusId, Long userId);
    //Lay chi tiet doi tuong like
    Optional<StatusLike> findByStatusIdAndUserId(Long statusId, Long userId);
    //thuc hien unlike
    void deleteByStatusIdAndUserId(Long statusId, Long userId);
}
