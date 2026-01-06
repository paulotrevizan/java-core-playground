package com.trevizan.javacoreplayground.service;

import com.trevizan.javacoreplayground.model.User;
import com.trevizan.javacoreplayground.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getEmail() == null) {
            throw new RuntimeException("User name and email are required.");
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        return user.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
