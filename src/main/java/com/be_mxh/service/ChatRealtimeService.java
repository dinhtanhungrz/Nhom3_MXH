package com.be_mxh.service;

import com.be_mxh.dto.chat.ChatGroupResponse;
import com.be_mxh.dto.chat.MessageResponse;

public interface ChatRealtimeService {
    void publishGroupCreated(ChatGroupResponse groupResponse);

    void publishMessageCreated(MessageResponse messageResponse);
}
