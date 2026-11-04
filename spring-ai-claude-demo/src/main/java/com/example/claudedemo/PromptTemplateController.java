package com.example.claudedemo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/prompt-template")
public class PromptTemplateController {

    private static final String DEFAULT_TEMPLATE =
            "You are a {role}. Explain {topic} to a {audience} in exactly {count} bullet points.";
    private static final String DEFAULT_VARIABLES = """
            role=chef
            topic=photosynthesis
            audience=5-year-old
            count=3""";

    private final ChatClient chatClient;

    public PromptTemplateController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("template", DEFAULT_TEMPLATE);
        model.addAttribute("variables", DEFAULT_VARIABLES);
        return "prompt-template";
    }

    @PostMapping("/run")
    public String run(@RequestParam String template,
                      @RequestParam(defaultValue = "") String variables,
                      Model model) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Prompt prompt = promptTemplate.create(parseVariables(variables));
            model.addAttribute("rendered", prompt.getContents());
            model.addAttribute("reply", chatClient.prompt(prompt).call().content());
        } catch (Exception e) {
            model.addAttribute("error", "Template call failed: " + e.getMessage());
        }
        model.addAttribute("template", template);
        model.addAttribute("variables", variables);
        return "prompt-template";
    }

    private Map<String, Object> parseVariables(String input) {
        return input.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && line.contains("="))
                .map(line -> line.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> pair[0].trim(),
                        pair -> pair[1].trim(),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }
}
