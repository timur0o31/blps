package org.example.blps.config;

import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
@Profile("!worker")
public class SuperUserInitializer implements CommandLineRunner {
// CommandLineRunner это простой Spring Boot интерфейс с методом run.
// Spring Boot автоматически вызывает метод run для всех бинов, которые имплиментируют этот интерфейс, при инициализации контекста.

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final String usersFilePath;

    @Autowired
    public SuperUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                AdminRepository adminRepository,
                                @Value("${users.file.path}") String usersFilePath) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.usersFilePath = usersFilePath;
    }

    @Override
    public void run(String... args) throws Exception {
        Path lockPath = Path.of(usersFilePath + ".init.lock");
        Path lockParent = lockPath.getParent();
        if (lockParent != null) {
            Files.createDirectories(lockParent);
        }
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = channel.lock()) {
            User user = new User();
            user.setName("Admin");
            user.setSurname("Admin");
            user.setEmail("admin@gmail.com");
            user.setPassword(passwordEncoder.encode("Gufi2001"));
            user.setPhoneNumber("+70000000000");
            user.setRole(Role.ADMIN);
            user.setSuperUser(true);
            if (!userRepository.existsByEmail(user.getEmail())) {
                userRepository.saveUser(user);
            } else {
                user = userRepository.findByEmail(user.getEmail())
                        .orElseThrow(() -> new IllegalStateException("Не удалось найти superuser после проверки существования"));
            }
            if (adminRepository.findByUserId(user.getId()).isEmpty()) {
                Admin admin = new Admin();
                admin.setUserId(user.getId());
                admin.setAccountState(true);
                adminRepository.save(admin);
            }
        }
    }
}
