package com.CVRgram.Controller;

import com.CVRgram.Model.Message;
import com.CVRgram.Model.User;
import com.CVRgram.Repository.MessageRepository;
import com.CVRgram.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/chat")
    public String chatPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "forward:/chat.html";
    }

    // 🔹 API: Fetch people the current user is FOLLOWING
    @GetMapping("/api/chat/following")
    @ResponseBody
    public ResponseEntity<List<User>> getFollowingForChat(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return ResponseEntity.status(401).build();

        User currentUser = userRepository.findByUsername(sessionUser.getUsername()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<User> following = currentUser.getFollowingIds().stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());

        // Hide passwords for security
        following.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(following);
    }

    // 🔹 API: Fetch Chat History between current user and another user
    @GetMapping("/api/chat/history/{otherUserId}")
    @ResponseBody
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long otherUserId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return ResponseEntity.status(401).build();

        User currentUser = userRepository.findByUsername(sessionUser.getUsername()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<Message> history = messageRepository.findConversation(currentUser.getId(), otherUserId);
        return ResponseEntity.ok(history);
    }

    // 🔹 WEBSOCKET PRIVATE MESSAGE HANDLING
    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload MessageRequest request) {
        User sender = userRepository.findByUsername(request.getSenderUsername()).orElse(null);
        User recipient = userRepository.findById(request.getRecipientId()).orElse(null);

        if (sender != null && recipient != null) {
            System.out.println("CHAT: " + sender.getUsername() + " -> " + recipient.getUsername() + " [" + request.getContent() + "]");

            // BYPASS CHECK FOR DEBUGGING
            Message message = new Message(sender, recipient, request.getContent());
            messageRepository.save(message);

            Long minId = Math.min(sender.getId(), recipient.getId());
            Long maxId = Math.max(sender.getId(), recipient.getId());
            String topic = "/topic/messages/" + minId + "_" + maxId;

            System.out.println("BROADCASTING TO: " + topic);
            messagingTemplate.convertAndSend(topic, message);

            // 🔹 GLOBAL NOTIFICATION: Send to the recipient's personal notification topic
            String notificationTopic = "/topic/notifications/" + recipient.getId();
            System.out.println("NOTIFY: Sending to recipient " + recipient.getUsername() + " (ID: " + recipient.getId() + ") on topic: " + notificationTopic);
            messagingTemplate.convertAndSend(notificationTopic, message);
        }
    }

    // DTO for incoming requests
    @lombok.Data
    public static class MessageRequest {
        private String senderUsername;
        private Long recipientId;
        private String content;
    }
}