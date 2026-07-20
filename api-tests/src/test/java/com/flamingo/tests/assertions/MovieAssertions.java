package com.flamingo.tests.assertions;

import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import org.flamingo.responses.graphql.Movies.Movie;

import static org.assertj.core.api.Assertions.assertThat;

@AllArgsConstructor
public final class MovieAssertions {

    @Step("Assert that movie has correct schema")
    public static void assertMovieHasCorrectSchema(Movie movie) {
        assertThat(movie).as("movie").isNotNull();
        assertThat(movie.getId()).as("movie.id").isNotBlank();
        assertThat(movie.getTitle()).as("movie.title").isNotBlank();
    }
}
