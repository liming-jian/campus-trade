package com.campus_trade.websocket;

import com.campus_trade.dto.MessageDTO;
import com.campus_trade.entity.Message;
import com.campus_trade.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionMap.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }

        String payload = textMessage.getPayload();
        @SuppressWarnings("unchecked")
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);

        Long conversationId = msgMap.get("conversationId") != null
                ? Long.valueOf(msgMap.get("conversationId").toString()) : null;
        Long receiverId = msgMap.get("receiverId") != null
                ? Long.valueOf(msgMap.get("receiverId").toString()) : null;
        String content = msgMap.get("content") != null ? msgMap.get("content").toString() : null;

        if (conversationId == null || receiverId == null || content == null) {
            return;
        }

        Message message = chatService.sendMessage(conversationId, userId, receiverId, content);

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());

        String jsonMsg = objectMapper.writeValueAsString(dto);
        sendMessage(userId, jsonMsg);
        sendMessage(receiverId, jsonMsg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionMap.remove(userId);
        }
    }

    private void sendMessage(Long userId, String message) {
        WebSocketSession session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                // log error, ignore
            }
        }
    }

    public void pushToUser(Long userId, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            sendMessage(userId, json);
        } catch (Exception e) {
            // ignore
        }
    }

    public void notifyReadReceipt(Long conversationId, Long readerId, Long senderId) {
        try {
            Map<String, Object> receipt = Map.of(
                "type", "read",
                "conversationId", conversationId,
                "readerId", readerId
            );
            String json = objectMapper.writeValueAsString(receipt);
            sendMessage(senderId, json);
        } catch (Exception e) {
            // ignore
        }
    }
}
