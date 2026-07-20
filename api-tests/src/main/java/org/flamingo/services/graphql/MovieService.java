package org.flamingo.services.graphql;

import io.qameta.allure.Step;
import lombok.RequiredArgsConstructor;
import org.flamingo.clients.GraphQLClient;
import org.flamingo.responses.graphql.Movies.Movie;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MovieService {

    private static final String MOVIES_PAGE_QUERY = "graphql/moviesPage.gql";
    private static final String MOVIE_BY_ID_QUERY = "graphql/movieById.gql";
    private static final String MOVIE_WITH_PUBLISHER_QUERY = "graphql/movieWithPublisher.gql";
    private static final String CREATE_MOVIE_MUTATION = "graphql/createMovie.gql";

    private final GraphQLClient client;

    @Step("Fetch movies page (first={first}, skip={skip})")
    public List<Movie> getPage(int first, int skip) {
        return client.executeGql(MOVIES_PAGE_QUERY, Map.of("first", first, "skip", skip))
                .then()
                .asList("movies", Movie.class);
    }

    @Step("Fetch movie by id={id}")
    public Movie getById(String id) {
        return client.executeGql(MOVIE_BY_ID_QUERY, Map.of("id", id))
                .then()
                .asPojo("movie", Movie.class);
    }

    @Step("Fetch movie by id={id} with publisher")
    public Movie getWithPublisher(String id) {
        return client.executeGql(MOVIE_WITH_PUBLISHER_QUERY, Map.of("id", id))
                .then()
                .asPojo("movie", Movie.class);
    }

    @Step("Create movie with title='{title}'")
    public Movie create(String title) {
        return client.executeGql(CREATE_MOVIE_MUTATION, Map.of("title", title))
                .then()
                .asPojo("createMovie", Movie.class);
    }
}
