package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.chat.ChatGroupResponse;
import com.be_mxh.dto.chat.CreateChatGroupRequest;
import com.be_mxh.entity.ChatGroup;
import com.be_mxh.entity.ChatGroupMember;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.ChatGroupMemberRepository;
import com.be_mxh.repository.ChatGroupRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.ChatRealtimeService;
import com.be_mxh.service.ChatGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ChatGroupServiceImpl implements ChatGroupService {

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
    public ChatGroupResponse createChatGroup(CreateChatGroupRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();

        // Validate request
        if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
            throw new BadRequestException("TÃªn nhÃ³m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new BadRequestException("NhÃ³m pháº£i cÃ³ Ã­t nháº¥t 1 thÃ nh viÃªn");
        }

        // Láº¥y thÃ´ng tin ngÆ°á»i táº¡o nhÃ³m
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Táº¡o group chat má»›i
        ChatGroup chatGroup = ChatGroup.builder()
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .creator(creator)
                .build();

        chatGroup = chatGroupRepository.save(chatGroup);

        // ThÃªm creator lÃ m admin
        ChatGroupMember creatorMember = ChatGroupMember.builder()
                .chatGroup(chatGroup)
                .user(creator)
                .role(ChatGroupMember.MemberRole.ADMIN)
                .build();
        chatGroupMemberRepository.save(creatorMember);

        // ThÃªm cÃ¡c thÃ nh viÃªn khÃ¡c
        Set<Long> uniqueMemberIds = new HashSet<>(request.getMemberIds());
        uniqueMemberIds.remove(currentUserId); // TrÃ¡nh thÃªm creator 2 láº§n

        for (Long memberId : uniqueMemberIds) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng cÃ³ ID " + memberId + " khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

            ChatGroupMember groupMember = ChatGroupMember.builder()
                    .chatGroup(chatGroup)
                    .user(member)
                    .role(ChatGroupMember.MemberRole.MEMBER)
                    .build();
            chatGroupMemberRepository.save(groupMember);
        }

        // Reload Ä‘á»ƒ láº¥y members
        ChatGroup savedGroup = chatGroupRepository.findByIdWithMembers(chatGroup.getId())
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        ChatGroupResponse response = ChatGroupResponse.fromEntity(savedGroup);
        chatRealtimeService.publishGroupCreated(response);
        return response;
    }

    @Override
    public Page<ChatGroupResponse> getUserChatGroups(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        Page<ChatGroup> groups = chatGroupRepository.findAllGroupsByUserId(userId, pageable);
        return groups.map(ChatGroupResponse::fromEntity);
    }

    @Override
    public ChatGroupResponse getChatGroupById(Long groupId) {
        Long userId = securityUtils.getCurrentUserId();

        ChatGroup chatGroup = chatGroupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Kiá»ƒm tra quyá»n truy cáº­p
        if (!hasAccess(groupId, userId)) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p nhÃ³m nÃ y");
        }

        return ChatGroupResponse.fromEntity(chatGroup);
    }

    @Override
    @Transactional
    public ChatGroupResponse addMembersToGroup(Long groupId, List<Long> memberIds) {
        Long currentUserId = securityUtils.getCurrentUserId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Kiá»ƒm tra xem ngÆ°á»i dÃ¹ng cÃ³ pháº£i lÃ  admin khÃ´ng
        ChatGroupMember currentMember = chatGroupMemberRepository.findByChatGroupAndUser(chatGroup, 
                userRepository.findById(currentUserId).orElseThrow())
                .orElseThrow(() -> new BadRequestException("Báº¡n khÃ´ng pháº£i lÃ  thÃ nh viÃªn cá»§a nhÃ³m nÃ y"));

        if (!currentMember.getRole().equals(ChatGroupMember.MemberRole.ADMIN)) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n thÃªm thÃ nh viÃªn");
        }

        // ThÃªm cÃ¡c thÃ nh viÃªn má»›i
        for (Long memberId : memberIds) {
            // Kiá»ƒm tra xem thÃ nh viÃªn Ä‘Ã£ tá»“n táº¡i khÃ´ng
            if (chatGroupMemberRepository.isMemberOfGroup(groupId, memberId)) {
                continue;
            }

            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng cÃ³ ID " + memberId + " khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

            ChatGroupMember groupMember = ChatGroupMember.builder()
                    .chatGroup(chatGroup)
                    .user(member)
                    .role(ChatGroupMember.MemberRole.MEMBER)
                    .build();
            chatGroupMemberRepository.save(groupMember);
        }

        // Reload Ä‘á»ƒ láº¥y members
        ChatGroup updatedGroup = chatGroupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        return ChatGroupResponse.fromEntity(updatedGroup);
    }

    @Override
    @Transactional
    public void removeMemberFromGroup(Long groupId, Long userId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        ChatGroupMember memberToRemove = chatGroupMemberRepository.findByChatGroupAndUser(chatGroup, user)
                .orElseThrow(() -> new ResourceNotFoundException("ThÃ nh viÃªn khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Chá»‰ admin hoáº·c chÃ­nh ngÆ°á»i Ä‘Ã³ má»›i cÃ³ thá»ƒ xÃ³a
        if (!currentUserId.equals(userId) && !currentUserId.equals(chatGroup.getCreator().getId())) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a thÃ nh viÃªn nÃ y");
        }

        chatGroupMemberRepository.delete(memberToRemove);
    }

    @Override
    @Transactional
    public void deleteChatGroup(Long groupId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ³m chat khÃ´ng Ä‘Æ°á»£c tÃ¬m tháº¥y"));

        // Chá»‰ creator má»›i cÃ³ thá»ƒ xÃ³a nhÃ³m
        if (!currentUserId.equals(chatGroup.getCreator().getId())) {
            throw new BadRequestException("Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a nhÃ³m nÃ y");
        }

        chatGroupRepository.delete(chatGroup);
    }

    @Override
    public boolean hasAccess(Long groupId, Long userId) {
        return chatGroupMemberRepository.isMemberOfGroup(groupId, userId);
    }
}
