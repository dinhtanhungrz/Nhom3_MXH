package com.be_mxh.service.impl;

import com.be_mxh.dto.chat.ChatGroupMemberResponse;
import com.be_mxh.dto.chat.ChatGroupResponse;
import com.be_mxh.dto.chat.MessageResponse;
import com.be_mxh.repository.ChatGroupMemberRepository;
import com.be_mxh.service.ChatRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRealtimeServiceImpl implements ChatRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatGroupMemberRepository chatGroupMemberRepository;

    @Override
    public void publishGroupCreated(ChatGroupResponse groupResponse) {
        for (ChatGroupMemberResponse member : groupResponse.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/queue/chat-groups",
                    groupResponse
            );
        }
    }

    @Override
    public void publishMessageCreated(MessageResponse messageResponse) {
        messagingTemplate.convertAndSend(
                "/topic/chat-groups/" + messageResponse.getChatGroupId(),
                messageResponse
        );

        for (String username : chatGroupMemberRepository.findMemberUsernamesByChatGroupId(messageResponse.getChatGroupId())) {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/messages",
                    messageResponse
            );
        }
    }
}
