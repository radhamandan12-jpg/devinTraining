package com.training.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoanApprovalAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApprovalAssistantApplication.class, args);
    }
}
