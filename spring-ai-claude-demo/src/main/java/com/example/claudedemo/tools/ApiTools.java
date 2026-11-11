package com.example.claudedemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ApiTools {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .build();

    @Tool(description = "Fetch a blog post by its id from the JSONPlaceholder REST API")
    public String fetchPost(@ToolParam(description = "Post id, 1-100") int id) {
        return restClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .body(String.class);
    }

    @Tool(description = "Fetch a user profile by id from the JSONPlaceholder REST API")
    public String fetchUser(@ToolParam(description = "User id, 1-10") int id) {
        return restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(String.class);
    }

    @Tool(description = "List to-do items for a user from the JSONPlaceholder REST API")
    public String fetchTodos(@ToolParam(description = "User id, 1-10") int userId) {
        return restClient.get()
                .uri("/todos?userId={userId}&_limit=5", userId)
                .retrieve()
                .body(String.class);
    }
}
