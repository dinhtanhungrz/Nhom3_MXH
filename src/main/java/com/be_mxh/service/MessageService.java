package com.be_mxh.service;

import com.be_mxh.dto.chat.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    
    /**
     * Gá»­i tin nháº¯n
     */
    MessageResponse sendMessage(Long groupId, String content);

    MessageResponse sendMessage(Long groupId, String content, String username);

    /**
     * Láº¥y danh sÃ¡ch tin nháº¯n cá»§a nhÃ³m (phÃ¢n trang)
     */
    Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable);

    /**
     * Chá»‰nh sá»­a tin nháº¯n
     */
    MessageResponse editMessage(Long messageId, String content);

    /**
     * XÃ³a tin nháº¯n
     */
    void deleteMessage(Long messageId);
}
