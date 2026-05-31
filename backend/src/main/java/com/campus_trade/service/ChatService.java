package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.dto.ConversationDTO;
import com.campus_trade.dto.MessageDTO;
import com.campus_trade.entity.Conversation;
import com.campus_trade.entity.Message;
import com.campus_trade.entity.User;
import com.campus_trade.mapper.ConversationMapper;
import com.campus_trade.mapper.MessageMapper;
import com.campus_trade.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    public ChatService(ConversationMapper conversationMapper, MessageMapper messageMapper, UserMapper userMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Long getOrCreateConversation(Long user1Id, Long user2Id) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Conversation::getUser1Id, user1Id).eq(Conversation::getUser2Id, user2Id)
                .or()
                .eq(Conversation::getUser1Id, user2Id).eq(Conversation::getUser2Id, user1Id));
        Conversation conversation = conversationMapper.selectOne(wrapper);
        if (conversation != null) {
            return conversation.getId();
        }

        Long u1 = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        Long u2 = user1Id.compareTo(user2Id) < 0 ? user2Id : user1Id;
        conversation = new Conversation();
        conversation.setUser1Id(u1);
        conversation.setUser2Id(u2);
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    public List<ConversationDTO> getConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, userId)
                .or()
                .eq(Conversation::getUser2Id, userId)
                .orderByDesc(Conversation::getLastMessageAt);
        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> targetUserIds = new HashSet<>();
        for (Conversation conv : conversations) {
            Long targetId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
            targetUserIds.add(targetId);
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!targetUserIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(targetUserIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        List<ConversationDTO> result = new ArrayList<>();
        for (Conversation conv : conversations) {
            ConversationDTO dto = new ConversationDTO();
            Long targetId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
            dto.setConversationId(conv.getId());
            dto.setTargetUserId(targetId);
            dto.setLastMessage(conv.getLastMessage());
            dto.setLastMessageAt(conv.getLastMessageAt());

            User targetUser = userMap.get(targetId);
            if (targetUser != null) {
                dto.setTargetNickname(targetUser.getNickname());
                dto.setTargetAvatar(targetUser.getAvatar());
            }

            Long unreadCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getConversationId, conv.getId())
                            .eq(Message::getReceiverId, userId)
                            .eq(Message::getIsRead, 0));
            dto.setUnreadCount(unreadCount.intValue());

            result.add(dto);
        }
        return result;
    }

    @Transactional
    public List<MessageDTO> getMessages(Long conversationId, Long userId, Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .orderByDesc(Message::getCreatedAt);
        Page<Message> messagePage = messageMapper.selectPage(pageParam, wrapper);

        List<Message> messages = messagePage.getRecords();
        List<Long> unreadIds = messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && (m.getIsRead() == null || m.getIsRead() == 0))
                .map(Message::getId)
                .collect(Collectors.toList());

        if (!unreadIds.isEmpty()) {
            Message update = new Message();
            update.setIsRead(1);
            LambdaQueryWrapper<Message> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(Message::getId, unreadIds);
            messageMapper.update(update, updateWrapper);
        }

        Collections.reverse(messages);

        return messages.stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(m.getId());
            dto.setConversationId(m.getConversationId());
            dto.setSenderId(m.getSenderId());
            dto.setReceiverId(m.getReceiverId());
            dto.setContent(m.getContent());
            dto.setIsRead(unreadIds.contains(m.getId()) ? 1 : m.getIsRead());
            dto.setCreatedAt(m.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public void markAsRead(Long conversationId, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);
        List<Message> unreadMessages = messageMapper.selectList(wrapper);
        if (!unreadMessages.isEmpty()) {
            Message update = new Message();
            update.setIsRead(1);
            LambdaQueryWrapper<Message> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(Message::getId, unreadMessages.stream().map(Message::getId).collect(Collectors.toList()));
            messageMapper.update(update, updateWrapper);
        }
    }

    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        messageMapper.insert(message);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        return message;
    }
}
