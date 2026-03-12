package com.be_mxh.repository;

import com.be_mxh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MutualFriendsRepository extends JpaRepository<User, Long> {

    @Query("""
                SELECT u
                FROM User u
                WHERE EXISTS (
                    SELECT 1
                    FROM Friendship f1
                    WHERE f1.status = 'ACCEPTED'
                      AND (
                          (f1.requester.id = :u1 AND f1.addressee = u)
                       OR (f1.addressee.id = :u1 AND f1.requester = u)
                      )
                )
                AND EXISTS (
                    SELECT 1
                    FROM Friendship f2
                    WHERE f2.status = 'ACCEPTED'
                      AND (
                          (f2.requester.id = :u2 AND f2.addressee = u)
                       OR (f2.addressee.id = :u2 AND f2.requester = u)
                      )
                )
            """)
    Page<User> findMutualFriends(@Param("u1") Long u1, @Param("u2") Long u2, Pageable pageable);
}