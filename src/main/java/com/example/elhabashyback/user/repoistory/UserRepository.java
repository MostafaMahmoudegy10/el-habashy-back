package com.example.elhabashyback.user.repoistory;

import com.example.elhabashyback.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

   Optional<Users> findByEmailIgnoreCase(String email);

   boolean existsByEmailIgnoreCase(String email);
}
