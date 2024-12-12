package com.backend.ticketingsystem.repository;

import com.backend.ticketingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

