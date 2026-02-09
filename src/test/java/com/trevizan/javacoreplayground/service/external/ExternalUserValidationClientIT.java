package com.trevizan.javacoreplayground.service.external;

import org.assertj.core.api.Assertions;
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
            .isInstanceOf(Exception.class);
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
                client.validate("Paulo", "paulo@trevizan.com"))
            .isInstanceOf(Exception.class);

        boolean result = client.validate("Paulo", "paulo@trevizan.com");
        Assertions.assertThat(result).isTrue();
    }

}
