package com.be_mxh.repository;

import com.be_mxh.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    List<Status> findByActiveTrueOrderByCreatedAtDesc();

    @Query("""
      SELECT p FROM Status p
      WHERE p.active = false
      AND p.user.id IN (
        SELECT f.requester.id FROM Friendship f
        WHERE f.requester.id= :userId
      )
      ORDER BY p.createdAt DESC
    """)

    List<Status> feedByFollow(Long userId);

    int countByUserId(Long userId);

    List<Status> findAllByContentContaining(String query);

    @Query("""
    SELECT s FROM Status s
    WHERE s.user.id = :ownerId
    AND s.active = true
    AND (
        s.visibility = 'PUBLIC'
        OR s.user.id = :viewerId
        OR (
            s.visibility = 'FRIENDS_ONLY'
            AND EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = 'ACCEPTED'
                AND (
                    (f.requester.id = :viewerId AND f.addressee.id = :ownerId)
                    OR
                    (f.requester.id = :ownerId AND f.addressee.id = :viewerId)
                )
            )
        )
    )
    ORDER BY s.createdAt DESC
""")
    List<Status> findVisibleStatuses(
            @Param("ownerId") Long ownerId,
            @Param("viewerId") Long viewerId
    );

}
