package com.example.claudedemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mcp")
public class McpController {

    private static final String SYSTEM = """
            You are a helpful assistant connected to MCP servers.
            - Filesystem tools expose the project directory on this machine.
            - GitHub tools expose the user's GitHub account.
            Use the tools to answer, and say which server/tool you used.""";

    private final ChatClient chatClient;

    public McpController(ChatClient.Builder builder, ToolCallbackProvider mcpToolCallbacks) {
        this.chatClient = builder
                .defaultToolCallbacks(mcpToolCallbacks)
                .build();
    }

    @GetMapping
    public String page() {
        return "mcp";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String prompt, Model model) {
        try {
            model.addAttribute("reply",
                    chatClient.prompt().system(SYSTEM).user(prompt).call().content());
        } catch (Exception e) {
            model.addAttribute("error", "MCP call failed: " + e.getMessage());
        }
        model.addAttribute("prompt", prompt);
        return "mcp";
    }
}
