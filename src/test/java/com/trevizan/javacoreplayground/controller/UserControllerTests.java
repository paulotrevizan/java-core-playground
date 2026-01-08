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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(userService.createUser(request)).thenReturn(userCreated);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
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

}
