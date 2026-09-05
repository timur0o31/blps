package org.example.blps.config;

import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.service.CamundaIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperUserInitializer implements CommandLineRunner {
// CommandLineRunner это простой Spring Boot интерфейс с методом run.
// Spring Boot автоматически вызывает метод run для всех бинов, которые имплиментируют этот интерфейс, при инициализации контекста.

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final CamundaIdentityService camundaIdentityService;

    @Autowired
    public SuperUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                AdminRepository adminRepository, CamundaIdentityService camundaIdentityService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.camundaIdentityService = camundaIdentityService;
    }

    @Override
    public void run(String... args) throws Exception {
        String rawPassword = "Gufi2001";
        User user = new User();
        user.setName("Admin");
        user.setSurname("Admin");
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber("+70000000000");
        user.setRole(Role.ADMIN);
        user.setSuperUser(true);
        if (!userRepository.existsByEmail(user.getEmail())) {
            userRepository.saveUser(user);
            Admin admin = new Admin();
            admin.setUserId(user.getId());
            admin.setAccountState(true);
            adminRepository.save(admin);
        } else {
            user = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new IllegalStateException("Не удалось загрузить суперпользователя"));
        }
        camundaIdentityService.createUser(user, rawPassword, "ADMIN");
        camundaIdentityService.createUser(user, rawPassword, "camunda-admin");
    }
}
