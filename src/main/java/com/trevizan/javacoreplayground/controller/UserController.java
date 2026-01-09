package com.trevizan.javacoreplayground.controller;

import com.trevizan.javacoreplayground.controller.dto.UserRequest;
import com.trevizan.javacoreplayground.controller.dto.UserResponse;
import com.trevizan.javacoreplayground.model.User;
import com.trevizan.javacoreplayground.service.UserService;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserRequest request) {
        User user = new User(null, request.name(), request.email());
        User userCreated = userService.createUser(user);

        URI location = URI.create("/api/v1/users/" + userCreated.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return UserResponse.from(userService.getUserById(id));
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
            .stream()
            .map(UserResponse::from)
            .toList();
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = new User(id, request.name(), request.email());
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
