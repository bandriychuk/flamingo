package com.flamingo.tests;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.aeonbits.owner.ConfigFactory;
import org.flamingo.ProjectConfig;
import org.flamingo.conditions.Conditions;
import org.flamingo.payloads.auth.AuthPayload;
import org.flamingo.responses.rest.auth.AuthResponse;
import org.flamingo.services.ApiServices;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiTests {

    protected static final ProjectConfig config = ConfigFactory.create(ProjectConfig.class, System.getProperties());
    protected ApiServices api;
    protected Faker faker = new Faker();
    protected RuntimeState runtimeState = new RuntimeState();

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = config.baseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setUpEach() {
       api = new ApiServices(config.baseGraphQLUrl());
    }

    @AfterEach
    void tearDown() {
        clearAccessToken();
    }

    protected String loginAsDefaultUser() {
        return loginAs("admin", "password123");
    }

    protected String loginAs(String username, String password) {
        AuthPayload authPayload = new AuthPayload();
        authPayload.setUsername(username);
        authPayload.setPassword(password);

        AuthResponse authResponse = api.rest().authService()
                .login(authPayload)
                .shouldHave(Conditions.statusCode(200))
                .asPojo(AuthResponse.class);

        String accessToken = authResponse.getToken();

        assertThat(accessToken).as("Access token").isNotBlank();

        api.rest().authService().setAuthToken(accessToken);
        return accessToken;
    }


    protected void clearAccessToken() {
        api.rest().authService().clearAuthToken();
    }

}
