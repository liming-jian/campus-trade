package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.campus_trade.entity.Conversation;
import com.campus_trade.entity.Message;
import com.campus_trade.mapper.ConversationMapper;
import com.campus_trade.mapper.MessageMapper;
import com.campus_trade.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class ChatServiceTest {

    @Test
    void markAsReadUpdatesUnreadMessagesForReceiver() {
        ConversationMapper conversationMapper = mock(ConversationMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        MessageMapper messageMapper = mock(MessageMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        UserMapper userMapper = mock(UserMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        ChatService chatService = new ChatService(conversationMapper, messageMapper, userMapper);

        Message unreadMessage = new Message();
        unreadMessage.setId(9L);
        unreadMessage.setConversationId(3L);
        unreadMessage.setReceiverId(2L);
        unreadMessage.setIsRead(0);
        doAnswer((Answer<List<Message>>) invocation -> List.of(unreadMessage))
                .when(messageMapper).selectList(any(Wrapper.class));

        chatService.markAsRead(3L, 2L);

        verify(messageMapper).update(any(Message.class), any(Wrapper.class));
    }

    @Test
    void sendMessageCreatesUnreadMessageAndRefreshesConversationPreview() {
        ConversationMapper conversationMapper = mock(ConversationMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        MessageMapper messageMapper = mock(MessageMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        UserMapper userMapper = mock(UserMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        ChatService chatService = new ChatService(conversationMapper, messageMapper, userMapper);

        Message message = chatService.sendMessage(3L, 1L, 2L, "你好");

        assertEquals(0, message.getIsRead());
        verify(messageMapper).insert(message);
        verify(conversationMapper).updateById(any(Conversation.class));
    }
}
