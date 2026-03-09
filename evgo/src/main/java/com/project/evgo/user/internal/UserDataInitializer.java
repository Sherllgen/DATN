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
        initStationOwner();
        initRegularUser();
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
        String stationOwnerEmail = "stationowner@evgo.com";

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

        if (!userRepository.existsByEmail(stationOwnerEmail)) {
            Role stationOwnerRole = roleRepository.findByName("STATION_OWNER")
                    .orElseThrow(() -> new RuntimeException("STATION_OWNER role not found"));

            User stationOwner = new User();
            stationOwner.setEmail(stationOwnerEmail);
            stationOwner.setPassword(passwordEncoder.encode("StationOwner@123"));
            stationOwner.setFullName("Station Owner");
            stationOwner.setStatus(UserStatus.ACTIVE);
            stationOwner.setEmailVerified(true);
            stationOwner.setPhoneVerified(false);
            stationOwner.setAuthProvider(AuthProvider.LOCAL);
            stationOwner.setRoles(Set.of(stationOwnerRole));

            userRepository.save(stationOwner);
            log.info("Created default station owner user: {}", stationOwnerEmail);
        }
    }

    private void initStationOwner() {
        String ownerEmail = "station.owner@evgo.com";

        if (!userRepository.existsByEmail(ownerEmail)) {
            Role ownerRole = roleRepository.findByName("STATION_OWNER")
                    .orElseThrow(() -> new RuntimeException("STATION_OWNER role not found"));

            User owner = new User();
            owner.setEmail(ownerEmail);
            owner.setPassword(passwordEncoder.encode("Owner@123"));
            owner.setFullName("Test Station Owner");
            owner.setPhoneNumber("0901234567");
            owner.setStatus(UserStatus.ACTIVE);
            owner.setEmailVerified(true);
            owner.setPhoneVerified(true);
            owner.setAuthProvider(AuthProvider.LOCAL);
            owner.setRoles(Set.of(ownerRole));

            userRepository.save(owner);
            log.info("Created default station owner user: {}", ownerEmail);
        }
    }

    private void initRegularUser() {
        String userEmail = "user@gmail.com";

        if (!userRepository.existsByEmail(userEmail)) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            User regularUser = new User();
            regularUser.setEmail(userEmail);
            regularUser.setPassword(passwordEncoder.encode("User@123"));
            regularUser.setFullName("Test Regular User");
            regularUser.setPhoneNumber("0987654321");
            regularUser.setStatus(UserStatus.ACTIVE);
            regularUser.setEmailVerified(true);
            regularUser.setPhoneVerified(true);
            regularUser.setAuthProvider(AuthProvider.LOCAL);
            regularUser.setRoles(Set.of(userRole));

            userRepository.save(regularUser);
            log.info("Created default regular user: {}", userEmail);
        }
    }
}
