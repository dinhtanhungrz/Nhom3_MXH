package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.chat.MessageResponse;
import com.be_mxh.entity.ChatGroup;
import com.be_mxh.entity.Message;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.ChatGroupMemberRepository;
import com.be_mxh.repository.ChatGroupRepository;
import com.be_mxh.repository.MessageRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.ChatRealtimeService;
import com.be_mxh.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private ChatGroupMemberRepository chatGroupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private ChatRealtimeService chatRealtimeService;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long groupId, String content) {
        Long senderId = securityUtils.getCurrentUserId();
        User currentSender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("NgÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃƒÂ¬m thÃ¡ÂºÂ¥y"));

        // Kiá»ƒm tra group tá»“n táº¡i
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Kiá»ƒm tra ngÆ°á»i dÃ¹ng cÃ³ quyá»n gá»­i tin nháº¯n khÃ´ng
        if (!chatGroupMemberRepository.isMemberOfGroup(groupId, senderId)) {
            throw new BadRequestException("Báº¡n khÃ´ng pháº£i lÃ  thÃ nh viÃªn cá»§a nhÃ³m nÃ y");
        }

        // Kiá»ƒm tra content
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Ná»™i dung tin nháº¯n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Táº¡o message
        Message message = Message.builder()
                .chatGroup(chatGroup)
                .sender(sender)
                .content(content.trim())
                .build();

        message = messageRepository.save(message);

        MessageResponse response = toMessageResponse(message);
        chatRealtimeService.publishMessageCreated(response);
        return response;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long groupId, String content, String username) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("NgÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃƒÂ¬m thÃ¡ÂºÂ¥y"));

        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("NÃ¡Â»â„¢i dung tin nhÃ¡ÂºÂ¯n khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng");
        }

        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃƒÂ³m chat khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃƒÂ¬m thÃ¡ÂºÂ¥y"));

        if (!chatGroupMemberRepository.isMemberOfGroup(groupId, sender.getId())) {
            throw new BadRequestException("BÃ¡ÂºÂ¡n khÃƒÂ´ng phÃ¡ÂºÂ£i lÃƒÂ  thÃƒÂ nh viÃƒÂªn cÃ¡Â»Â§a nhÃƒÂ³m nÃƒÂ y");
        }

        Message message = Message.builder()
                .chatGroup(chatGroup)
                .sender(sender)
                .content(content.trim())
                .build();

        message = messageRepository.save(message);

        MessageResponse response = toMessageResponse(message);
        chatRealtimeService.publishMessageCreated(response);
        return response;
    }

    @Override
    public Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();

        // Kiá»ƒm tra group tá»“n táº¡i
        chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Kiá»ƒm tra ngÆ°á»i dÃ¹ng cÃ³ quyá»n truy cáº­p khÃ´ng
        if (!chatGroupMemberRepository.isMemberOfGroup(groupId, userId)) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n xem tin nháº¯n cá»§a nhÃ³m nÃ y");
        }

        Page<Message> messages = messageRepository.findByChatGroupIdOrderByCreatedAtDesc(groupId, pageable);
        return messages.map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public MessageResponse editMessage(Long messageId, String content) {
        Long userId = securityUtils.getCurrentUserId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin nháº¯n khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Chá»‰ ngÆ°á»i gá»­i má»›i cÃ³ thá»ƒ chá»‰nh sá»­a
        if (!userId.equals(message.getSender().getId())) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n chá»‰nh sá»­a tin nháº¯n nÃ y");
        }

        if (message.isDeleted()) {
            throw new BadRequestException("KhÃ´ng thá»ƒ chá»‰nh sá»­a tin nháº¯n Ä‘Ã£ bá»‹ xÃ³a");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Ná»™i dung tin nháº¯n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        message.setContent(content.trim());
        message.setEdited(true);

        message = messageRepository.save(message);

        return toMessageResponse(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Long userId = securityUtils.getCurrentUserId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin nháº¯n khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Chá»‰ ngÆ°á»i gá»­i hoáº·c admin nhÃ³m má»›i cÃ³ thá»ƒ xÃ³a
        if (!userId.equals(message.getSender().getId()) && !userId.equals(message.getChatGroup().getCreator().getId())) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a tin nháº¯n nÃ y");
        }

        message.setDeleted(true);
        messageRepository.save(message);
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatGroupId(message.getChatGroup().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .edited(message.isEdited())
                .deleted(message.isDeleted())
                .build();
    }
}
