package com.be_mxh.repository;

import com.be_mxh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MutualFriendsRepository extends JpaRepository<User, Long> {

    @Query("""
    SELECT u FROM User u
    WHERE u.id IN (
        SELECT CASE
            WHEN f1.requester.id = :u1 THEN f1.addressee.id
            ELSE f1.requester.id
        END
        FROM Friendship f1
        WHERE (f1.requester.id = :u1 OR f1.addressee.id = :u1)
          AND f1.status = 'ACCEPTED'
    )
    AND u.id IN (
        SELECT CASE
            WHEN f2.requester.id = :u2 THEN f2.addressee.id
            ELSE f2.requester.id
        END
        FROM Friendship f2
        WHERE (f2.requester.id = :u2 OR f2.addressee.id = :u2)
          AND f2.status = 'ACCEPTED'
    )
    """)
    Page<User> findMutualFriends(@Param("u1") Long u1,
                                 @Param("u2") Long u2,
                                 Pageable pageable);
}