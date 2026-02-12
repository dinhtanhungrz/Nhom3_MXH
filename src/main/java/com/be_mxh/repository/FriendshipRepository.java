package com.be_mxh.repository;

import com.be_mxh.entity.Friendship;
import com.be_mxh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Tìm bạn chung giữa User A (myId) và User B (targetId).
     * Logic: Lấy danh sách bạn của A GIAO với danh sách bạn của B.
     */
    @Query(value = """
        SELECT u.* FROM users u
        WHERE u.id IN (
            -- Lấy danh sách ID bạn bè của TÔI (myId)
            SELECT CASE 
                WHEN f.requester_id = :myId THEN f.addressee_id 
                ELSE f.requester_id 
            END
            FROM friendships f
            WHERE (f.requester_id = :myId OR f.addressee_id = :myId)
            AND f.status = 'ACCEPTED'
        )
        AND u.id IN (
            -- Lấy danh sách ID bạn bè của NGƯỜI KIA (targetId)
            SELECT CASE 
                WHEN f2.requester_id = :targetId THEN f2.addressee_id 
                ELSE f2.requester_id 
            END
            FROM friendships f2
            WHERE (f2.requester_id = :targetId OR f2.addressee_id = :targetId)
            AND f2.status = 'ACCEPTED'
        )
        """,
            countQuery = """
        SELECT COUNT(*) FROM users u
        WHERE u.id IN (
            SELECT CASE WHEN f.requester_id = :myId THEN f.addressee_id ELSE f.requester_id END
            FROM friendships f
            WHERE (f.requester_id = :myId OR f.addressee_id = :myId) AND f.status = 'ACCEPTED'
        )
        AND u.id IN (
            SELECT CASE WHEN f2.requester_id = :targetId THEN f2.addressee_id ELSE f2.requester_id END
            FROM friendships f2
            WHERE (f2.requester_id = :targetId OR f2.addressee_id = :targetId) AND f2.status = 'ACCEPTED'
        )
        """,
            nativeQuery = true)
    Page<User> findCommonFriends(@Param("myId") Long myId,
                                 @Param("targetId") Long targetId,
                                 Pageable pageable);
    @Query(value = """
    SELECT u.* FROM users u
    JOIN friendships f ON (u.id = f.requester_id OR u.id = f.addressee_id)
    WHERE (f.requester_id = :userId OR f.addressee_id = :userId)
    AND f.status = 'ACCEPTED'
    AND u.id != :userId
    """,
            nativeQuery = true)
    Page<User> findAllFriendsByUserId(@Param("userId") Long userId, Pageable pageable);
}