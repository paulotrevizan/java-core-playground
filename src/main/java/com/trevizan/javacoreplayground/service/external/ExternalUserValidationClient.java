package com.trevizan.javacoreplayground.service.external;

import com.trevizan.javacoreplayground.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

import java.net.SocketTimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalUserValidationClient {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ExternalUserValidationClient(
        RestTemplate restTemplate,
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry
    ) {
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-user-validation");
        this.retry = retryRegistry.retry("external-user-validation");
    }

    public boolean validate(String name, String email) {
        return Decorators.ofSupplier(() -> doValidate(name, email))
            .withRetry(retry)
            .withCircuitBreaker(circuitBreaker)
            .get();
    }

    private boolean doValidate(String name, String email) {
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
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw new IllegalArgumentException(
                    "Invalid request to external service: " + ex.getStatusCode()
                );
            }

            throw new ExternalServiceException(
                "External service returned error: " + ex.getStatusCode(),
                ex
            );
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
