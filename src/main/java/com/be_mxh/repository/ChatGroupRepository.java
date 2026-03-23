package com.be_mxh.repository;

import com.be_mxh.entity.ChatGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    
    /**
     * TÃ¬m táº¥t cáº£ cÃ¡c nhÃ³m mÃ  ngÆ°á»i dÃ¹ng lÃ  thÃ nh viÃªn
     */
    @Query("""
        SELECT DISTINCT cg FROM ChatGroup cg 
        JOIN cg.members m 
        WHERE m.user.id = :userId 
        ORDER BY cg.updatedAt DESC
    """)
    Page<ChatGroup> findAllGroupsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * TÃ¬m nhÃ³m chat theo ID vá»›i eager load members
     */
    @Query("""
        SELECT DISTINCT cg FROM ChatGroup cg 
        LEFT JOIN FETCH cg.members m 
        WHERE cg.id = :groupId
    """)
    Optional<ChatGroup> findByIdWithMembers(@Param("groupId") Long groupId);

    /**
     * TÃ¬m cÃ¡c nhÃ³m Ä‘Æ°á»£c táº¡o bá»Ÿi ngÆ°á»i dÃ¹ng
     */
    List<ChatGroup> findByCreatorId(Long creatorId);
}
