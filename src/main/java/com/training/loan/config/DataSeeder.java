package com.training.loan.config;

import com.training.loan.domain.AppUser;
import com.training.loan.domain.enums.Role;
import com.training.loan.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures at least one loan officer account exists so the manual
 * review queue is operable on a fresh deployment.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.officer-username}")
    private String officerUsername;

    @Value("${app.seed.officer-password}")
    private String officerPassword;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(officerUsername).isEmpty()) {
            userRepository.save(new AppUser(officerUsername,
                    passwordEncoder.encode(officerPassword), "Loan Officer", Role.OFFICER));
            log.info("Seeded officer account '{}'", officerUsername);
        }
    }
}
