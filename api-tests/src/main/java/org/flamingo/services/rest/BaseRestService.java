package org.flamingo.services.rest;

import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flamingo.clients.HttpSpecs;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseRestService {

    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();
    private static final String AUTH_HEADER_NAME = "Cookie";
    private static final String AUTH_COOKIE_PREFIX = "token=";

    private final HttpSpecs httpSpecs;

    protected RequestSpecification setUp() {
        return auth(httpSpecs.jsonSpec());
    }

    protected RequestSpecification setUpWithAuth(String token) {
       return setUp()
                .when()
                .spec(authRequestSpecification(token));
    }

    protected RequestSpecification auth(RequestSpecification spec) {
        String authToken = ACCESS_TOKEN.get();
        if (authToken == null || authToken.isBlank()) {
            return spec;
        }
        return spec.header(AUTH_HEADER_NAME, authToken);
    }

    public void setAuthToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is null or blank");
        }
        ACCESS_TOKEN.set(formatAuthToken(token));
    }

    public void clearAuthToken() {
        ACCESS_TOKEN.remove();
    }

    private String formatAuthToken(String token) {
        return token.startsWith(AUTH_COOKIE_PREFIX) ? token : AUTH_COOKIE_PREFIX + token;
    }

    @Step
    public RequestSpecification authRequestSpecification(String authToken) {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader(AUTH_HEADER_NAME, formatAuthToken(authToken))
                .build();
    }
}
