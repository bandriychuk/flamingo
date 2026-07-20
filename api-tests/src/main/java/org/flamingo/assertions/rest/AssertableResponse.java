package org.flamingo.assertions.rest;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.flamingo.conditions.Condition;

@RequiredArgsConstructor
@Slf4j
public class AssertableResponse {

    private final Response response;

    public <T> T asPojo(Class<T> clazz) {
        return response.as(clazz);
    }

    public Response response() {
        return response;
    }

    @Step("Assert response should have: '{0}'")
    public AssertableResponse shouldHave(Condition condition) {
        log.info("Asserting status code {}", condition);
        condition.check(response);
        return this;
    }
}
