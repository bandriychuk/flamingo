package org.flamingo.services.rest;

import io.qameta.allure.Step;
import org.flamingo.assertions.rest.AssertableResponse;
import org.flamingo.clients.HttpSpecs;
import org.flamingo.payloads.auth.AuthPayload;

public class AuthService extends BaseRestService {

    public AuthService(HttpSpecs httpSpecs) {
        super(httpSpecs);
    }

    @Step
    public AssertableResponse login(AuthPayload userPayload) {
        return new AssertableResponse(
                setUp()
                        .when()
                        .body(userPayload)
                        .post("/auth")
                        .then()
                        .extract()
                        .response());
    }
}
