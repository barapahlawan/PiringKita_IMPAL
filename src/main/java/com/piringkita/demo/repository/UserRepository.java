package com.piringkita.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.piringkita.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
