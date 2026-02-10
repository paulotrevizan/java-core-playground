package com.trevizan.javacoreplayground.service.external;

import com.trevizan.javacoreplayground.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.net.SocketTimeoutException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@SpringBootTest
@EnableWireMock(
    @ConfigureWireMock(port = 8099)
)
class ExternalUserValidationClientIT {

    @Autowired
    private ExternalUserValidationClient client;

    @Autowired
    private CircuitBreaker externalUserValidationCircuitBreaker;

    @BeforeEach
    void resetCircuitBreaker() {
        externalUserValidationCircuitBreaker.reset();
    }

    @Test
    void shouldReturnTrueWhenExternalServiceReturnsValidUser() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")
            )
        );

        boolean result = client.validate("Paulo", "paulo@trevizan.com");
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void shouldThrowExceptionOnServerError() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)));

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldSimulateIntermittentServerError() {
        stubFor(post("/api/v1/external/users/validate")
            .inScenario("Intermittent error")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("NextCallSuccess"));

        stubFor(post("/api/v1/external/users/validate")
            .inScenario("Intermittent error")
            .whenScenarioStateIs("NextCallSuccess")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")));

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);

        boolean result = client.validate("Paulo", "paulo@trevizan.com");
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void shouldTimeoutWhenExternalServiceIsSlow() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withFixedDelay(5000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")));

        Assertions.assertThatThrownBy(() ->
            client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class)
            .hasCauseInstanceOf(SocketTimeoutException.class);
    }

    @Test
    void shouldThrowExceptionWhenExternalServiceReturnsEmptyBody() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")));

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class)
            .hasMessageContaining("Empty");
    }

    @Test
    void shouldThrowExceptionOnClientError() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(400)));

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldReturnTrueWhenServiceIsUp() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")
            )
        );

        boolean result = client.validate("Paulo", "paulo@trevizan.com");
        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(externalUserValidationCircuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldOpenCircuitAfterFailures() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)));

        for (int i = 0; i < 4; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
        }

        Assertions.assertThat(externalUserValidationCircuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.OPEN);

        Assertions.assertThatThrownBy(() ->
            client.validate("Paulo", "paulo@trevizan.com")
        ).isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void shouldCloseCircuitAfterSuccessfulHalfOpenCall() throws InterruptedException {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)));

        for (int i = 0; i < 4; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
        }

        Assertions.assertThat(externalUserValidationCircuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(5100);

        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")
            )
        );

        boolean result = client.validate("Paulo", "paulo@trevizan.com");
        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(externalUserValidationCircuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.CLOSED);
    }

}
