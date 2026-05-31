package com.campus_trade.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ChatWebSocketHandlerTest {

    @Test
    void notifyReadReceiptPushesReadEventToSenderSession() throws Exception {
        ChatWebSocketHandler handler = new ChatWebSocketHandler(null);
        WebSocketSession session = mock(WebSocketSession.class, withSettings().mockMaker("mock-maker-subclass"));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);

        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/chat"));
        handler.afterConnectionEstablished(session);

        handler.notifyReadReceipt(3L, 2L, 1L);

        verify(session).sendMessage(argThat(message -> {
            String payload = ((TextMessage) message).getPayload();
            return payload.contains("\"type\":\"read\"")
                    && payload.contains("\"conversationId\":3")
                    && payload.contains("\"readerId\":2");
        }));
    }
}
