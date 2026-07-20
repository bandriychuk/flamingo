package org.flamingo.clients;

import io.restassured.response.Response;
import org.flamingo.assertions.graphql.GraphQLResponse;

import static org.flamingo.utils.graphql.GqlUtils.readGql;

public class GraphQLClient {

    private final String url;
    private final HttpSpecs httpSpecs;

    public GraphQLClient(String url, HttpSpecs httpSpecs) {
        this.url = url;
        this.httpSpecs = httpSpecs;
    }

    private Response runQuery(GraphQLQuery query) {
        return httpSpecs.jsonSpec().body(query).post(url);
    }

    public GraphQLResponse executeGql(String name) {
        var query = readGql(name);
        return new GraphQLResponse(runQuery(query));
    }

    public GraphQLResponse executeGql(String name, Object variables) {
        var query = readGql(name, variables);
        return new GraphQLResponse(runQuery(query));
    }

}
