package com.be_mxh.repository;

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

  List<Status> findStatusByUserIdOrderByCreatedAtDesc(Long userId);
  List<Status> findStatusByUserId(Long userId);

  @Query("""
        SELECT s FROM Status s
        WHERE s.user.id = :userId
          AND s.visibility = :visibility
          AND s.active = true
        ORDER BY s.createdAt DESC
    """)
  Page<Status> findPublicStatusesByUser(
    @Param("userId") Long userId,
    @Param("visibility") Status.Visibility visibility,
    Pageable pageable
  );

  // Dùng cho: GET /api/statuses/query?query=... (toàn mạng, có lọc visibility)
  @Query("""
        SELECT s FROM Status s
        WHERE s.active = true
        AND s.user.id != :viewerId
        AND LOWER(s.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND (
            s.visibility = 'PUBLIC'
            OR (
                s.visibility = 'FRIENDS_ONLY'
                AND EXISTS (
                    SELECT f FROM Friendship f
                    WHERE f.status = 'ACCEPTED'
                    AND (
                        (f.requester.id = :viewerId AND f.addressee.id = s.user.id)
                        OR
                        (f.requester.id = s.user.id AND f.addressee.id = :viewerId)
                    )
                )
            )
        )
        ORDER BY s.createdAt DESC
    """)
  List<Status> globalSearch(
    @Param("viewerId") Long viewerId,
    @Param("keyword") String keyword);

  @Query("""
        SELECT s FROM Status s
        WHERE s.user.id = :ownerId
        AND s.active = true
        AND LOWER(s.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
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
  List<Status> searchVisibleStatuses(
    @Param("ownerId") Long ownerId,
    @Param("viewerId") Long viewerId,
    @Param("keyword") String keyword);

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
    @Param("viewerId") Long viewerId);

  @Query("""
        SELECT s FROM Status s
        WHERE s.active = true
        AND (
            (s.user.id = :viewerId)
            OR (s.visibility = 'PUBLIC')
            OR (
                s.visibility = 'FRIENDS_ONLY'
                AND s.user.id IN (
                    SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :viewerId AND f.status = 'ACCEPTED'
                    UNION
                    SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :viewerId AND f.status = 'ACCEPTED'
                )
            )
        )
        ORDER BY s.createdAt DESC LIMIT 10
    """)
  List<Status> getNewsfeedStatuses(@Param("viewerId") Long viewerId);
  @Query("""
  SELECT s FROM Status s
  WHERE s.visibility = 'PUBLIC'
  AND s.active = true
  ORDER BY s.createdAt DESC
    """)
  List<Status> findGuestFeed();
}
