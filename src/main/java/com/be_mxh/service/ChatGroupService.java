package com.be_mxh.service;

import com.be_mxh.dto.chat.ChatGroupResponse;
import com.be_mxh.dto.chat.CreateChatGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatGroupService {
    
    /**
     * Táº¡o nhÃ³m chat má»›i
     */
    ChatGroupResponse createChatGroup(CreateChatGroupRequest request);

    /**
     * Láº¥y danh sÃ¡ch nhÃ³m cá»§a ngÆ°á»i dÃ¹ng hiá»‡n táº¡i
     */
    Page<ChatGroupResponse> getUserChatGroups(Pageable pageable);

    /**
     * Láº¥y thÃ´ng tin chi tiáº¿t nhÃ³m chat
     */
    ChatGroupResponse getChatGroupById(Long groupId);

    /**
     * ThÃªm thÃ nh viÃªn vÃ o nhÃ³m
     */
    ChatGroupResponse addMembersToGroup(Long groupId, java.util.List<Long> memberIds);

    /**
     * XÃ³a thÃ nh viÃªn khá»i nhÃ³m
     */
    void removeMemberFromGroup(Long groupId, Long userId);

    /**
     * XÃ³a nhÃ³m chat
     */
    void deleteChatGroup(Long groupId);

    /**
     * Kiá»ƒm tra ngÆ°á»i dÃ¹ng cÃ³ quyá»n truy cáº­p nhÃ³m khÃ´ng
     */
    boolean hasAccess(Long groupId, Long userId);
}
