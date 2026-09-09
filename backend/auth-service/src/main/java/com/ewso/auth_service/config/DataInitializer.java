package com.ewso.auth_service.config;

import com.ewso.auth_service.entity.Role;
import com.ewso.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MANAGER");
        createRoleIfNotExists("TEAM_LEAD");
        createRoleIfNotExists("PROJECT_MANAGER");
        createRoleIfNotExists("EMPLOYEE");
    }

    private void createRoleIfNotExists(String roleName) {

        if (!roleRepository.existsByName(roleName)) {

            Role role = Role.builder()
                    .name(roleName)
                    .build();

            roleRepository.save(role);
        }
    }
}
