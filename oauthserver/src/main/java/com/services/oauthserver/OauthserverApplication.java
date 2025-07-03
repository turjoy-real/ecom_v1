package com.services.oauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.services.oauthserver.repositories.UserRepo;
import com.services.oauthserver.repositories.RoleRepository;
import com.services.oauthserver.models.User;
import com.services.oauthserver.models.Role;
import java.util.List;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableAsync
public class OauthserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthserverApplication.class, args);
	}

	@Bean
	public CommandLineRunner createAdminUser(
			@Value("${admin.email}") String adminEmail,
			@Value("${admin.password}") String adminPassword,
			UserRepo userRepo,
			RoleRepository roleRepository,
			BCryptPasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepo.findByEmail(adminEmail).isEmpty()) {
				Role adminRole = roleRepository.findByName("ADMIN")
						.orElseGet(() -> {
							Role newRole = new Role();
							newRole.setName("ADMIN");
							return roleRepository.save(newRole);
						});
				User admin = new User();
				admin.setEmail(adminEmail);
				admin.setName("Admin");
				admin.setHashedPassword(passwordEncoder.encode(adminPassword));
				admin.setRoles(List.of(adminRole));
				admin.setEmailVerified(true);
				userRepo.save(admin);
				System.out.println("✅ Admin user created: " + adminEmail);
			} else {
				System.out.println("✅ Admin user already exists: " + adminEmail);
			}
		};
	}

}
