package com.be_mxh.repository;

import com.be_mxh.entity.Friendship;
import com.be_mxh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    @Query(value = "SELECT count(f) from Friendship f where f.status = 'ACCEPTED' AND (:userId = f.requester.id OR :userId = f.addressee.id)")
    Integer countFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :u1 AND f.addressee.id = :u2) OR (f.requester.id = :u2 AND f.addressee.id = :u1)")
    Optional<Friendship> findRelationship(@Param("u1") Long user1, @Param("u2") Long user2);

    @Query("""
                SELECT u
                FROM User u
                WHERE EXISTS (
                    SELECT 1
                    FROM Friendship f
                    WHERE f.status = 'ACCEPTED'
                      AND (
                            (f.requester.id = :userId AND f.addressee = u)
                         OR (f.addressee.id = :userId AND f.requester = u)
                      )
                )
            """)
    Page<User> findFriends(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT COUNT(f) > 0 FROM Friendship f
    WHERE f.status = 'ACCEPTED'
    AND (
        (f.requester.id = :a AND f.addressee.id = :b)
        OR  (f.requester.id = :b AND f.addressee.id = :a)
    )
    """)
    boolean existsAcceptedFriendship(@Param("a") Long a, @Param("b") Long b);
    // 1. Lấy lời mời kết bạn (PENDING)
    @Query("""
        SELECT f
        FROM Friendship f
        WHERE f.addressee.id = :userId
          AND f.status = 'PENDING'
    """)
    List<Friendship> findPendingRequests(@Param("userId") Long userId);

    // 2. Tìm request pending cụ thể
    @Query("""
        SELECT f
        FROM Friendship f
        WHERE f.requester.id = :requesterId
          AND f.addressee.id = :addresseeId
          AND f.status = 'PENDING'
    """)
    Optional<Friendship> findPendingRequest(
            @Param("requesterId") Long requesterId,
            @Param("addresseeId") Long addresseeId
    );

    // 3. Check tồn tại quan hệ bằng user_low/user_high (QUAN TRỌNG)
    Optional<Friendship> findByUserLowAndUserHigh(Long userLow, Long userHigh);

    // 4. Check đã gửi request chưa (PENDING)
    @Query("""
        SELECT COUNT(f) > 0
        FROM Friendship f
        WHERE f.status = 'PENDING'
        AND (
            (f.requester.id = :a AND f.addressee.id = :b)
         OR (f.requester.id = :b AND f.addressee.id = :a)
        )
    """)
    boolean existsPendingBetween(@Param("a") Long a, @Param("b") Long b);
    @Query("""
        SELECT CASE 
            WHEN f.requester.id = :userId THEN f.addressee.id 
            ELSE f.requester.id 
        END 
        FROM Friendship f 
        WHERE f.requester.id = :userId OR f.addressee.id = :userId
    """)
    List<Long> findAllRelatedUserIds(@Param("userId") Long userId);
}
