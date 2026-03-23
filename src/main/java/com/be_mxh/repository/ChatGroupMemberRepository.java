package com.be_mxh.repository;

import com.be_mxh.entity.ChatGroupMember;
import com.be_mxh.entity.ChatGroup;
import com.be_mxh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, Long> {
    
    /**
     * Tìm membership theo nhóm và người dùng
     */
    Optional<ChatGroupMember> findByChatGroupAndUser(ChatGroup chatGroup, User user);

    /**
     * Tìm tất cả thành viên của một nhóm
     */
    List<ChatGroupMember> findByChatGroupId(Long chatGroupId);

    @Query("""
        SELECT m.user.username
        FROM ChatGroupMember m
        WHERE m.chatGroup.id = :groupId
    """)
    List<String> findMemberUsernamesByChatGroupId(@Param("groupId") Long groupId);

    /**
     * Kiểm tra xem người dùng có phải là thành viên của nhóm không
     */
    @Query("""
        SELECT COUNT(m) > 0 
        FROM ChatGroupMember m 
        WHERE m.chatGroup.id = :groupId AND m.user.id = :userId
    """)
    boolean isMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * Xóa tất cả thành viên của một nhóm
     */
    void deleteByChatGroupId(Long chatGroupId);
}
