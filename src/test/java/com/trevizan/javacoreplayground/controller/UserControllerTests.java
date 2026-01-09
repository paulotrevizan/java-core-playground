package com.trevizan.javacoreplayground.controller;

import com.trevizan.javacoreplayground.exception.UserNotFoundException;
import com.trevizan.javacoreplayground.model.User;
import com.trevizan.javacoreplayground.service.UserService;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    final private User userCreated = new User(1L, "Anakin", "anakin@skywalker.com");

    @Test
    void shouldReturn200WhenCreatingUser() throws Exception {
        User request = new User(null, "Anakin", "anakin@skywalker.com");
        when(userService.createUser(any())).thenReturn(userCreated);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/users/1"));
    }

    @Test
    void shouldReturn200WhenGettingUserById() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userCreated);

        mockMvc.perform(get("/api/v1/users/{id}", userCreated.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Anakin"))
            .andExpect(jsonPath("$.email").value("anakin@skywalker.com"));
    }

    @Test
    void shouldReturn404WhenGettingUserById() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new UserNotFoundException(9999L));

        mockMvc.perform(get("/api/v1/users/{id}", 9999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenGettingAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userCreated));

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Anakin"))
            .andExpect(jsonPath("$[0].email").value("anakin@skywalker.com"))
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturn200WhenUpdatingUser() throws Exception {
        User updated = new User(1L, "Vader", "vader@empire.com");

        when(userService.updateUser(eq(1L), any(User.class)))
            .thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Vader"))
            .andExpect(jsonPath("$.email").value("vader@empire.com"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUser() throws Exception {
        when(userService.updateUser(eq(9999L), any(User.class)))
            .thenThrow(new UserNotFoundException(9999L));

        mockMvc.perform(put("/api/v1/users/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new User(null, "Paptimus Scirocco", "paptimus@newtype.com"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenDeletingUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        doThrow(new UserNotFoundException(1L)).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
            .andExpect(status().isNotFound());
    }

}
