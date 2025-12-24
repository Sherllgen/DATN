package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.AuthProvider;
import com.project.evgo.sharedkernel.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Data initializer for User module.
 * Seeds default roles and admin user on application startup.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        String[] roles = { "USER", "STATION_OWNER", "STAFF", "SUPER_ADMIN" };

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void initAdminUser() {
        String adminEmail = "admin@evgo.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFullName("System Administrator");
            admin.setStatus(UserStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setPhoneVerified(false);
            admin.setAuthProvider(AuthProvider.LOCAL);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
            log.info("Created default admin user: {}", adminEmail);
        }
    }
}
