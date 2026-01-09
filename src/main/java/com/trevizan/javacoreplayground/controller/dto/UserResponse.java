package com.trevizan.javacoreplayground.controller.dto;

import com.trevizan.javacoreplayground.model.User;

public record UserResponse(
    Long id,
    String name,
    String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
