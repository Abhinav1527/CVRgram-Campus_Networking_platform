package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.CVRgram.Service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 🔹 PAGE ROUTING
    @GetMapping
    public String chatPage() {
        return "forward:/chat.html";
    }

    // 🔹 API ENDPOINT
    @ResponseBody
    @GetMapping("/api")
    public String chatApi() {
        return chatService.getChat();
    }
}