package com.training.loan.repository;

import com.training.loan.domain.AppUser;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import com.training.loan.domain.enums.Role;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test against a real MySQL 8 instance.
 * Automatically skipped when Docker is not available.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoanApplicationRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired private LoanApplicationRepository applicationRepository;
    @Autowired private AppUserRepository userRepository;

    @Test
    void persistsAndQueriesApplicationsByStatus() {
        AppUser user = userRepository.save(
                new AppUser("customer1", "secret", "Customer One", Role.CUSTOMER));

        LoanApplication app = new LoanApplication();
        app.setApplicant(user);
        app.setRequestedAmount(new BigDecimal("30000"));
        app.setTermMonths(36);
        app.setLoanPurpose(LoanPurpose.AUTO);
        app.setAnnualIncome(new BigDecimal("80000"));
        app.setMonthlyDebtPayments(new BigDecimal("800"));
        app.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        app.setYearsEmployed(3);
        app.setCreditScore(700);
        app.setStatus(ApplicationStatus.MANUAL_REVIEW);
        applicationRepository.save(app);

        assertThat(applicationRepository
                .findByStatusOrderByCreatedAtAsc(ApplicationStatus.MANUAL_REVIEW))
                .hasSize(1);
        assertThat(applicationRepository.findByApplicantOrderByCreatedAtDesc(user))
                .extracting(LoanApplication::getLoanPurpose)
                .containsExactly(LoanPurpose.AUTO);
    }
}
