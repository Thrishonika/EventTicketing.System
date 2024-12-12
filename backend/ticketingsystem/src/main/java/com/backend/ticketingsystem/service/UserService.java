package com.backend.ticketingsystem.service;

import com.backend.ticketingsystem.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {


    private List<User> users = new ArrayList<>();

    // Retrieve all users
    public List<User> getAllUsers() {
        return users;
    }

    // Retrieve a user by ID
    public Optional<User> getUserById(Long id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }

    // Add a new user
    public User saveUser(User user) {
        users.add(user);
        return user;
    }

    // Delete a user by ID
    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
}



