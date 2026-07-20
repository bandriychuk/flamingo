package org.flamingo.assertions.graphql;

import io.restassured.response.Response;

public class GraphQLResponse {

    private final Response response;

    public GraphQLResponse(Response response) {
        this.response = response;
    }

    public AssertableGraphQLResponse then() {
        return new AssertableGraphQLResponse(response);
    }
}
