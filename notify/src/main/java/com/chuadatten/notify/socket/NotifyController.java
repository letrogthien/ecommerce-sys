// package com.chuadatten.notify.socket;

// import java.security.Principal;

// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.stereotype.Controller;

// @Controller
// public class NotifyController {

//     private final NotifyService notifyService;

//     public NotifyController(NotifyService notifyService) {
//         this.notifyService = notifyService;
//     }

//     // Client gửi message tới /app/notifyAll
//     @MessageMapping("/notifyAll")
//     public void notifyAll(String message) {
//         notifyService.pushToAll(message);
//     }

//     // Client gửi message tới /app/notifyMe
//     @MessageMapping("/notifyMe")
//     public void notifyMe(String message, Principal principal) {
//         notifyService.pushToUser(principal.getName(), message);
//     }
// }