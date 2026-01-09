package com.trevizan.javacoreplayground.service;

import com.trevizan.javacoreplayground.exception.InvalidUserException;
import com.trevizan.javacoreplayground.exception.UserNotFoundException;
import com.trevizan.javacoreplayground.model.User;
import com.trevizan.javacoreplayground.repository.UserRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getEmail() == null) {
            throw new InvalidUserException("User name and email are required.");
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
            () -> new UserNotFoundException(id)
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User user) {
        return userRepository.update(id, user)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        boolean userDeleted = userRepository.deleteById(id);
        if (!userDeleted) {
            throw new UserNotFoundException(id);
        }
    }

}
