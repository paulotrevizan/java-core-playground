package com.trevizan.javacoreplayground.service.external;

import com.trevizan.javacoreplayground.exception.ExternalServiceException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalUserValidationClient {

    private final RestClient restClient;

    public ExternalUserValidationClient(
        RestClient.Builder builder
    ) {
        this.restClient = builder
            .baseUrl("http://localhost:8099")
            .build();
    }

    public boolean validate(String name, String email) {
        ExternalUserValidationResponse response = restClient.post()
            .uri("/api/v1/external/users/validate")
            .body(new ExternalUserValidationRequest(name, email))
            .retrieve()
            .body(ExternalUserValidationResponse.class);

        if (response == null) {
            throw new ExternalServiceException(
                "External user validation returned empty response"
            );
        }

        return response.valid();
    }

}
