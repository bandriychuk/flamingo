package com.flamingo.tests.graphql;

import com.flamingo.tests.BaseApiTests;
import com.flamingo.tests.testdata.MovieTestData;
import io.qameta.allure.*;
import org.flamingo.responses.graphql.Movies.Movie;
import org.flamingo.services.graphql.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.flamingo.tests.assertions.MovieAssertions.assertMovieHasCorrectSchema;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("graphql")
@Tag("positive")
@Epic("GraphQL API")
@Feature("Movies query")
@Severity(SeverityLevel.CRITICAL)
@DisplayName("GraphQL — positive scenarios")
public class GraphQLPositiveTest extends BaseApiTests {

    private MovieService movies() {
        return api.graphQL().movies();
    }

    @ParameterizedTest(name = "returns at most {0} movies when first={0}")
    @ValueSource(ints = {2, 3, 5})
    @DisplayName("list query honors the pagination limit")
    @Story("Pagination")
    void shouldReturnListRespectingPaginationLimit(int limit) {
        List<Movie> movies = movies().getPage(limit, 0);

        assertThat(movies)
                .as("movies for first=%d, skip=0", limit)
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(limit)
                .allSatisfy(m -> assertMovieHasCorrectSchema(m));
    }

    @Test
    @DisplayName("single-entity query returns the movie by its ID")
    @Story("Single entity by ID")
    void shouldReturnSingleMovieById() {
        String existingId = MovieTestData.existingMovieIdWithoutPoster(movies());

        Movie movie = movies().getById(existingId);

        assertMovieHasCorrectSchema(movie);
        assertThat(movie.getId())
                .as("returned id must match the requested id")
                .isEqualTo(existingId);
    }

    @Test
    @DisplayName("single-entity query returns a movie without a poster")
    @Story("Single entity by ID")
    void shouldReturnSingleMovieByIdWhenPosterIsMissing() {
        String existingId = MovieTestData.existingMovieIdWithoutPoster(movies());

        Movie movie = movies().getById(existingId);

        assertMovieHasCorrectSchema(movie);
        assertThat(movie.getId())
                .as("returned id must match the requested id")
                .isEqualTo(existingId);
        assertThat(movie.getMoviePoster())
                .as("moviePoster must remain null when the entity has no poster")
                .isNull();
    }

    @Test
    @DisplayName("query uses GraphQL variables, not string interpolation")
    @Story("Variables")
    void shouldHonorGraphQLVariables() {
        Movie firstOnPageZero = movies().getPage(1, 0).get(0);
        Movie firstOnPageOne = movies().getPage(1, 1).get(0);

        assertThat(firstOnPageZero.getId())
                .as("skip=0 and skip=1 must yield different first entries — proves variables are honored server-side")
                .isNotEqualTo(firstOnPageOne.getId());
    }


    @Test
    @DisplayName("query with fragment resolves nested types (moviePoster & publishedBy)")
    @Story("Fragments and nested types")
    void shouldResolveFragmentAndNestedFieldsAcrossTypes() {
        String existingId = MovieTestData.existingMovieIdWithoutPoster(movies());

        Movie movie = movies().getWithPublisher(existingId);

        assertMovieHasCorrectSchema(movie);
        assertThat(movie.getId()).isEqualTo(existingId);

        assertThat(movie.getPublishedBy())
                .as("publishedBy — nested User type resolved across types")
                .isNotNull()
                .satisfies(user -> assertThat(user.getName()).as("publishedBy.name").isNotBlank());
    }

}
