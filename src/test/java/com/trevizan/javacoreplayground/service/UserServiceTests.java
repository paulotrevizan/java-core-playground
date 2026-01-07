package com.trevizan.javacoreplayground.service;

import com.trevizan.javacoreplayground.exception.InvalidUserException;
import com.trevizan.javacoreplayground.exception.UserNotFoundException;
import com.trevizan.javacoreplayground.model.User;
import com.trevizan.javacoreplayground.repository.UserRepository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTests {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    void shouldSaveUserSuccessfully() {
        User user = new User(null, "Char Aznable", "char@aznable.ze");
        User savedUser = userService.createUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Char Aznable");
        assertThat(savedUser.getEmail()).isEqualTo("char@aznable.ze");
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenNameOrEmailAreNull() {
        User userMissingName = new User(null, null, "test@email.com");
        User userMissingEmail = new User(null, "Username", null);

        assertThatThrownBy(() -> userService.createUser(userMissingName))
            .isInstanceOf(InvalidUserException.class)
            .hasMessageContaining("required");

        assertThatThrownBy(() -> userService.createUser(userMissingEmail))
            .isInstanceOf(InvalidUserException.class)
            .hasMessageContaining("required");
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        User user = new User(null, "Amuro Ray", "amuro@ray.ef");
        User savedUser = userService.createUser(user);

        User retrievedUser = userService.getUserById(savedUser.getId());

        assertThat(retrievedUser.getName()).isEqualTo("Amuro Ray");
        assertThat(retrievedUser.getEmail()).isEqualTo("amuro@ray.ef");
        assertThat(retrievedUser.getId()).isNotNull();
    }

    @Test
    void shouldGetAllUsersSuccessfully() {
        User user = new User(null, "Char", "char@aznable.ze");
        userService.createUser(user);

        User secondUser = new User(null, "Amuro", "amuro@ray.ef");
        userService.createUser(secondUser);

        List<User> savedUsers = userService.getAllUsers();
        assertThat(savedUsers).hasSize(2);
        assertThat(savedUsers.get(0).getName()).isEqualTo("Char");
        assertThat(savedUsers.get(0).getEmail()).isEqualTo("char@aznable.ze");
        assertThat(savedUsers.get(0).getId()).isNotNull();
        assertThat(savedUsers.get(1).getName()).isEqualTo("Amuro");
        assertThat(savedUsers.get(1).getEmail()).isEqualTo("amuro@ray.ef");
        assertThat(savedUsers.get(1).getId()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenUserWasNotFound() {
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("not found");
    }

}
