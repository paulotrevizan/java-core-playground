package com.trevizan.javacoreplayground.service.external;

import com.trevizan.javacoreplayground.exception.ExternalServiceException;

import java.net.SocketTimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalUserValidationClient {

    private final RestTemplate restTemplate;

    public ExternalUserValidationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validate(String name, String email) {
        try {
            ExternalUserValidationResponse response =
                restTemplate.postForObject(
                    "http://localhost:8099/api/v1/external/users/validate",
                    new ExternalUserValidationRequest(name, email),
                    ExternalUserValidationResponse.class
                );

            if (response == null) {
                throw new ExternalServiceException("Empty response from external service.");
            }

            return response.valid();
        } catch (ResourceAccessException ex) {
            throw mapResourceAccessException(ex);
        }
    }

    private ExternalServiceException mapResourceAccessException(ResourceAccessException ex) {
        if (ex.getCause() instanceof SocketTimeoutException) {
            return new ExternalServiceException("External service timeout.", ex);
        }
        return new ExternalServiceException("External service unreachable.", ex);
    }

}
