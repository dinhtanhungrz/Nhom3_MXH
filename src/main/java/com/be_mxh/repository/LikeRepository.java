package com.be_mxh.repository;

import com.be_mxh.entity.StatusLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<StatusLike, Long> {

    long countByStatusId(Long statusId);
}
