package com.trevizan.javacoreplayground.service.external;

public record ExternalUserValidationRequest(
    String name,
    String email
) { }
