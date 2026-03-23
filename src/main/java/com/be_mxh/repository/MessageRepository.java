package com.be_mxh.repository;

import com.be_mxh.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Tìm tất cả tin nhắn của một nhóm (phân trang)
     */
    @Query("""
        SELECT m FROM Message m 
        WHERE m.chatGroup.id = :groupId AND m.deleted = false
        ORDER BY m.createdAt DESC
    """)
    Page<Message> findByChatGroupIdOrderByCreatedAtDesc(
        @Param("groupId") Long groupId, 
        Pageable pageable
    );

    /**
     * Tìm tất cả tin nhắn của một nhóm (không phân trang)
     */
    @Query("""
        SELECT m FROM Message m 
        WHERE m.chatGroup.id = :groupId AND m.deleted = false
        ORDER BY m.createdAt ASC
    """)
    List<Message> findAllByChatGroupId(@Param("groupId") Long groupId);

    /**
     * Xóa tất cả tin nhắn của một nhóm
     */
    void deleteByChatGroupId(Long chatGroupId);
}
