package com.be_mxh.repository;

import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

//    List<Status> findByUserId(Long userId);
//
//    @Query("""
//        SELECT s FROM Status s
//        WHERE s.user.id = :userId
//          AND s.user.id IN (
//              SELECT CASE
//                WHEN f.requester.id = :userId THEN f.recipient.id
//                WHEN f.recipient.id = :userId THEN f.requester.id
//              END
//              FROM Friendship f
//              WHERE (f.requester.id = :userId OR f.recipient.id = :userId)
//                AND f.status = 'ACCEPTED'
//          )
//        ORDER BY s.createdAt DESC
//    """)
//    List<Status> findNewsFeed(@Param("userId") Long userId);
//
//    List<Status> findByUserIdAndVisibility(Long userId, Status.Visibility visibility);

    int countByUserId(Long userId);

    List<Status> findStatusByUserId(Long userId);

//
//    /**
//     * ✅ FIXED: Sử dụng @Query annotation với JPQL
//     * - Import đúng: org.springframework.data.domain.Pageable
//     * - Không cast type
//     * - Method name rõ ràng
//     */
//    @Query("""
//        SELECT s FROM Status s
//        WHERE s.user.id = :userId
//          AND s.visibility = :visibility
//          AND s.active = true
//        ORDER BY s.createdAt DESC
//    """)
//    Page<Status> findPublicStatusesByUser(
//            @Param("userId") Long userId,
//            @Param("visibility") Status.Visibility visibility,
//            Pageable pageable
//    );
}