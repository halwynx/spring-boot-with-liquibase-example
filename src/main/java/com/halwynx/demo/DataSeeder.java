package com.halwynx.demo;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.halwynx.demo.entity.UserEntity;
import com.halwynx.demo.repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepo userRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Data already exists, skipping seed");
            return;
        }
        seedData();
    }

    private void seedData() {
        log.info("Seeding data...");

        List<UserEntity> users = List.of(
            new UserEntity(null, "John Doe", "john.doe@example.com", null),
            new UserEntity(null, "Jane Smith", "jane.smith@example.com", null)
        );

        userRepo.saveAll(users);

        log.info("Seeded {} users", users.size());
    }
}