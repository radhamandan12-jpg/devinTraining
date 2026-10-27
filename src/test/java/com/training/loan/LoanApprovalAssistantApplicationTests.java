package com.training.loan;

import com.training.loan.ai.AiAssistantService;
import com.training.loan.ai.StubAiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LoanApprovalAssistantApplicationTests {

    @Autowired
    private AiAssistantService aiService;

    @Test
    void contextLoadsWithStubAiWhenNoApiKey() {
        assertThat(aiService).isInstanceOf(StubAiService.class);
    }
}
