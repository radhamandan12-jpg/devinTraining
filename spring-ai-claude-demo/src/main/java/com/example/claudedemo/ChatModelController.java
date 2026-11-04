package com.example.claudedemo;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/chat-model")
public class ChatModelController {

    private final ChatModel chatModel;

    public ChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping
    public String page() {
        return "chat-model";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String prompt,
                      @RequestParam(defaultValue = "") String systemPrompt,
                      @RequestParam(defaultValue = "0.7") double temperature,
                      @RequestParam(defaultValue = "1024") int maxTokens,
                      Model model) {
        List<Message> messages = new ArrayList<>();
        if (!systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        messages.add(new UserMessage(prompt));

        try {
            ChatResponse response = chatModel.call(new Prompt(messages,
                    AnthropicChatOptions.builder()
                            .temperature(temperature)
                            .maxTokens(maxTokens)
                            .build()));

            model.addAttribute("reply", response.getResult().getOutput().getText());
            model.addAttribute("modelName", response.getMetadata().getModel());
            model.addAttribute("promptTokens", response.getMetadata().getUsage().getPromptTokens());
            model.addAttribute("completionTokens", response.getMetadata().getUsage().getCompletionTokens());
            model.addAttribute("totalTokens", response.getMetadata().getUsage().getTotalTokens());
        } catch (Exception e) {
            model.addAttribute("error", "Model call failed: " + e.getMessage());
        }
        model.addAttribute("prompt", prompt);
        model.addAttribute("systemPrompt", systemPrompt);
        model.addAttribute("temperature", temperature);
        model.addAttribute("maxTokens", maxTokens);
        return "chat-model";
    }
}
