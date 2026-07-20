package org.flamingo.assertions.graphql;

import io.restassured.response.Response;
import org.hamcrest.Matcher;

import java.util.List;

public class AssertableGraphQLResponse {

    private static final String DATA = "data.";

    private final Response response;

    public AssertableGraphQLResponse(Response response) {
        this.response = response;
    }

    public void body(String jsonPath, Matcher<?> matcher) {
        response.then().body(DATA + jsonPath, matcher);
    }

    public <T> List<T> asList(String jsonPath, Class<T> tClass) {
        return response.then().extract().jsonPath().getList(DATA + jsonPath, tClass);
    }

    public <T> T asPojo(String jsonPath, Class<T> ref) {
        return response.then().extract().jsonPath().getObject(DATA + jsonPath, ref);
    }

    public List<String> errorMessages() {
        return response.then().extract().jsonPath().getList("errors.message", String.class);
    }

    public int statusCode() {
        return response.getStatusCode();
    }

}
