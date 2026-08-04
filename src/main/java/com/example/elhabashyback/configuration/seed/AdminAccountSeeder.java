package com.example.elhabashyback.configuration.seed;

import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminAccountSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;

    public AdminAccountSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin.enabled:true}") boolean enabled,
            @Value("${app.seed.admin.first-name:Mostafa}") String firstName,
            @Value("${app.seed.admin.last-name:Mahmoud}") String lastName,
            @Value("${app.seed.admin.email:mostafa.mahmoudegy10@gmail.com}") String email,
            @Value("${app.seed.admin.password:password}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Users admin = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(Users::new);

        admin.setFirstName(firstName.trim());
        admin.setLastName(lastName.trim());
        admin.setEmail(normalizedEmail);
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        if (admin.getPassword() == null || !passwordEncoder.matches(password, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(admin);
    }
}
