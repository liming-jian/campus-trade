package com.campus_trade.controller;

import com.campus_trade.common.Result;
import com.campus_trade.dto.ConversationDTO;
import com.campus_trade.dto.MessageDTO;
import com.campus_trade.entity.Conversation;
import com.campus_trade.entity.Message;
import com.campus_trade.mapper.ConversationMapper;
import com.campus_trade.service.ChatService;
import com.campus_trade.websocket.ChatWebSocketHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final ConversationMapper conversationMapper;
    private final ChatWebSocketHandler webSocketHandler;

    public ChatController(ChatService chatService, ConversationMapper conversationMapper, ChatWebSocketHandler webSocketHandler) {
        this.chatService = chatService;
        this.conversationMapper = conversationMapper;
        this.webSocketHandler = webSocketHandler;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/conversations")
    public Result<List<ConversationDTO>> getConversations() {
        Long userId = getCurrentUserId();
        List<ConversationDTO> list = chatService.getConversations(userId);
        return Result.ok(list);
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<MessageDTO>> getMessages(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        List<MessageDTO> messages = chatService.getMessages(id, userId, page, size);
        return Result.ok(messages);
    }

    @PostMapping("/conversations")
    public Result<Map<String, Long>> createConversation(@RequestBody Map<String, Long> body) {
        Long userId = getCurrentUserId();
        Long targetUserId = body.get("targetUserId");
        if (targetUserId == null) {
            return Result.error(400, "targetUserId is required");
        }
        Long conversationId = chatService.getOrCreateConversation(userId, targetUserId);
        return Result.ok(Map.of("conversationId", conversationId));
    }

    @PutMapping("/conversations/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        chatService.markAsRead(id, userId);

        // Notify conversation partner via WebSocket that messages were read
        Conversation conversation = conversationMapper.selectById(id);
        if (conversation != null) {
            Long partnerId = conversation.getUser1Id().equals(userId)
                    ? conversation.getUser2Id() : conversation.getUser1Id();
            webSocketHandler.notifyReadReceipt(id, userId, partnerId);
        }

        return Result.ok();
    }

    @PostMapping("/messages")
    public Result<MessageDTO> sendMessage(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (conversationId == null || content == null) {
            return Result.error(400, "conversationId and content are required");
        }

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return Result.error(400, "Conversation not found");
        }
        Long receiverId = conversation.getUser1Id().equals(userId)
                ? conversation.getUser2Id() : conversation.getUser1Id();

        Message message = chatService.sendMessage(conversationId, userId, receiverId, content);
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());

        // Push to both sender and receiver via WebSocket in real-time
        webSocketHandler.pushToUser(userId, dto);
        webSocketHandler.pushToUser(receiverId, dto);

        return Result.ok(dto);
    }
}
