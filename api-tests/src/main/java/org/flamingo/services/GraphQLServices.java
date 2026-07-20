package org.flamingo.services;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.flamingo.clients.GraphQLClient;
import org.flamingo.clients.HttpSpecs;
import org.flamingo.services.graphql.MovieService;

@Accessors(fluent = true)
@Getter
public class GraphQLServices {

    private final GraphQLClient client;
    private final MovieService movies;

    public GraphQLServices(String url, HttpSpecs httpSpecs) {
        this.client = new GraphQLClient(url, httpSpecs);
        this.movies = new MovieService(client);
    }
}
