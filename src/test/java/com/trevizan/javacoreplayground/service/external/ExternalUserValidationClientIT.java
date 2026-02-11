package com.trevizan.javacoreplayground.service.external;

import com.trevizan.javacoreplayground.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.net.SocketTimeoutException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@SpringBootTest
@EnableWireMock(
    @ConfigureWireMock(port = 8099)
)
class ExternalUserValidationClientIT {

    @Autowired
    private ExternalUserValidationClient client;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setup() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-user-validation");
        circuitBreaker.reset();
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
                .withStatus(500)
            )
        );

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldSimulateIntermittentServerError() {
        stubFor(post("/api/v1/external/users/validate")
            .inScenario("Intermittent error")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse()
                .withStatus(500)
            )
            .willSetStateTo("NextCallSuccess")
        );

        stubFor(post("/api/v1/external/users/validate")
            .inScenario("Intermittent error")
            .whenScenarioStateIs("NextCallSuccess")
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
    void shouldTimeoutWhenExternalServiceIsSlow() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withFixedDelay(5000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")
            )
        );

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class)
            .hasCauseInstanceOf(ResourceAccessException.class)
            .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }

    @Test
    void shouldThrowExceptionWhenExternalServiceReturnsEmptyBody() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")
            )
        );

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(ExternalServiceException.class)
            .hasMessageContaining("Empty");
    }

    @Test
    void shouldThrowExceptionOnClientBadRequestError() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(400)
            )
        );

        Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(IllegalArgumentException.class);

        verify(1, postRequestedFor(
            urlEqualTo("/api/v1/external/users/validate")
        ));
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
        Assertions.assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldOpenCircuitAfterFailures() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)
            )
        );

        for (int i = 0; i < 4; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
        }

        Assertions.assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.OPEN);

        Assertions.assertThatThrownBy(() ->
            client.validate("Paulo", "paulo@trevizan.com")
        ).isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void shouldCloseCircuitAfterSuccessfulHalfOpenCall() throws InterruptedException {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)
            )
        );

        for (int i = 0; i < 4; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
        }

        Assertions.assertThat(circuitBreaker.getState())
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
        Assertions.assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldRetryThreeTimesOnExternalServiceException() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)
            )
        );

        Assertions.assertThatThrownBy(() ->
            client.validate("Paulo", "paulo@trevizan.com")
        ).isInstanceOf(ExternalServiceException.class);

        verify(3, postRequestedFor(
            urlEqualTo("/api/v1/external/users/validate")
        ));
    }

    @Test
    void shouldOpenCircuitAfterFourFailedValidationsWithRetryEnabled() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)
            )
        );

        for (int i = 0; i < 4; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(ExternalServiceException.class);
        }

        Assertions.assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRegisterFailuresInCircuitBreakerMetrics() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(500)
            )
        );

        Assertions.assertThatThrownBy(() ->
            client.validate("Paulo", "paulo@trevizan.com")
        );

        int failedCalls = circuitBreaker
            .getMetrics()
            .getNumberOfFailedCalls();
        Assertions.assertThat(failedCalls).isEqualTo(1);
    }

    @Test
    void shouldNotOpenCircuitBreakerOnClientError() {
        stubFor(post("/api/v1/external/users/validate")
            .willReturn(aResponse()
                .withStatus(400)
            )
        );

        for (int i = 0; i < 10; i++) {
            Assertions.assertThatThrownBy(() ->
                client.validate("Paulo", "paulo@trevizan.com")
            ).isInstanceOf(IllegalArgumentException.class);
        }

        Assertions.assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.CLOSED);

        Assertions.assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls())
            .isEqualTo(0);
    }

}
