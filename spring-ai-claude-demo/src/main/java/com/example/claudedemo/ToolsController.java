package com.example.claudedemo;

import com.example.claudedemo.tools.ApiTools;
import com.example.claudedemo.tools.DbTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tools")
public class ToolsController {

    private static final String SYSTEM = """
            You are a helpful assistant with tools.
            - Use the database tools when the user wants to save, list or search notes.
            - Use the REST API tools when the user asks about posts, users or todos.
            - Answer plainly; if you used a tool, mention the result it returned.""";

    private final ChatClient chatClient;
    private final DbTools dbTools;
    private final ApiTools apiTools;

    public ToolsController(ChatClient.Builder builder, DbTools dbTools, ApiTools apiTools) {
        this.chatClient = builder.build();
        this.dbTools = dbTools;
        this.apiTools = apiTools;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("notes", dbTools.listNotes());
        return "tools";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String prompt, Model model) {
        try {
            String reply = chatClient.prompt()
                    .system(SYSTEM)
                    .user(prompt)
                    .tools(dbTools, apiTools)
                    .call()
                    .content();
            model.addAttribute("reply", reply);
        } catch (Exception e) {
            model.addAttribute("error", "Tool call failed: " + e.getMessage());
        }
        model.addAttribute("prompt", prompt);
        model.addAttribute("notes", dbTools.listNotes());
        return "tools";
    }
}
