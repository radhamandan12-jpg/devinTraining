package com.example.claudedemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.ai.anthropic.api-key=test-key")
class ClaudeDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
