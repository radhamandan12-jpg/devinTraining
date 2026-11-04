package com.example.claudedemo;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChatController {

    private static final String CONVERSATION_ID_ATTR = "conversationId";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultSystem("You are a helpful, concise assistant.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("history", chatMemory.get(conversationId(session)));
        return "index";
    }

    @PostMapping("/chat")
    public String chat(@RequestParam String message, HttpSession session,
                       RedirectAttributes redirectAttributes) {
        String conversationId = conversationId(session);
        try {
            chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Claude call failed: " + e.getMessage()
                            + " (is ANTHROPIC_API_KEY set?)");
        }
        return "redirect:/";
    }

    @PostMapping("/reset")
    public String reset(HttpSession session) {
        chatMemory.clear(conversationId(session));
        return "redirect:/";
    }

    private String conversationId(HttpSession session) {
        String id = (String) session.getAttribute(CONVERSATION_ID_ATTR);
        if (id == null) {
            id = UUID.randomUUID().toString();
            session.setAttribute(CONVERSATION_ID_ATTR, id);
        }
        return id;
    }
}
