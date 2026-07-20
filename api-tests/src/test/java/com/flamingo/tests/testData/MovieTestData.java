package com.flamingo.tests.testData;

import org.flamingo.responses.graphql.Movies.Movie;
import org.flamingo.services.graphql.MovieService;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public final class MovieTestData {

    private MovieTestData() {
    }

    private static final int SEARCH_WINDOW = 20;

    public static String existingMovieIdWithoutPoster(MovieService movies) {
        return findFirstMovieId(
                movies,
                m -> m.getMoviePoster() == null,
                "No movie without a poster"
        );
    }

    private static String findFirstMovieId(MovieService movies,
                                           Predicate<Movie> predicate,
                                           String missingReason) {
        var page = movies.getPage(SEARCH_WINDOW, 0);

        assertThat(page)
                .as("Precondition: dataset must contain at least one movie")
                .isNotEmpty();

        return page.stream()
                .filter(predicate)
                .map(Movie::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Precondition failed: %s in the first %d records."
                                .formatted(missingReason, SEARCH_WINDOW)));
    }
}
