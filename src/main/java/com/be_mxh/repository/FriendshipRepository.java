package com.be_mxh.repository;

import com.be_mxh.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    @Query(value = "SELECT count(f) from Friendship f where f.status = 'ACCEPTED' AND (:userId = f.requester.id OR :userId = f.addressee.id)")
    Integer countFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :u1 AND f.addressee.id = :u2) OR (f.requester.id = :u2 AND f.addressee.id = :u1)")
    Optional<Friendship> findRelationship(@Param("u1") Long user1, @Param("u2") Long user2);
}
