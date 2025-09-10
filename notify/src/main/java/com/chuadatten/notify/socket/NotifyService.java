package com.chuadatten.notify.socket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotifyService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotifyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Notify chung cho tất cả
    public void pushToAll(String message) {
        messagingTemplate.convertAndSend("/topic/notify", message);
    }

    // Notify riêng cho user
    public void pushToUser(String userId, String message) {
        messagingTemplate.convertAndSendToUser(
                userId,               // Principal.getName()
                "/queue/notify",      // client sub /user/queue/notify
                message
        );
    }
}
