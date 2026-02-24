package com.be_mxh.repository;

import com.be_mxh.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    List<Status> findByActiveTrueOrderByCreatedAtDesc();

    @Query("""
              SELECT p FROM Status p
              WHERE p.active = false
              AND p.user.id IN (
                SELECT f.requester.id FROM Friendship f
                WHERE f.requester= :userId
              )
              ORDER BY p.createdAt DESC
            """)
    List<Status> feedByFollow(Long userId);


    int countByUserId(Long userId);
    Page<Status> findByUserIdAndStatusAndActiveTrueOrderByCreatedAtDesc(
            Long userId,
            Status.Visibility status,
            Pageable pageable
    );
}
