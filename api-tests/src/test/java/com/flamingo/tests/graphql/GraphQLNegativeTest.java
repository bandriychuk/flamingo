package com.flamingo.tests.graphql;

import com.flamingo.tests.BaseApiTests;
import io.qameta.allure.*;
import org.flamingo.assertions.graphql.AssertableGraphQLResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;

@Tag("graphql")
@Tag("negative")
@Epic("GraphQL API")
@Feature("Error handling")
@Severity(SeverityLevel.NORMAL)
@DisplayName("GraphQL — negative scenarios")
public class GraphQLNegativeTest extends BaseApiTests {

    private static final String MOVIE_BY_ID_QUERY = "graphql/movieById.gql";
    private static final String MALFORMED_QUERY = "graphql/malformedQuery.gql";
    private static final String NON_EXISTENT_FIELD_QUERY = "graphql/nonExistentField.gql";

    private static final String VALID_LOOKING_BUT_MISSING_ID = "ckzzzzzzzzzzzzzzzzzzzzzz";

    @Test
    @DisplayName("valid but non-existent ID returns null data.movie and no errors")
    @Story("Not-found entity")
    void nonExistentIdShouldReturnNullMovieAndNoErrors() {
        AssertableGraphQLResponse response = api.graphQL().client()
                .executeGql(MOVIE_BY_ID_QUERY, Map.of("id", VALID_LOOKING_BUT_MISSING_ID))
                .then();

        assertThat(response.statusCode())
                .as("GraphQL returns HTTP 200 even for a not-found entity")
                .isEqualTo(200);

        response.body("movie", nullValue());

        assertThat(response.errorMessages())
                .as("no errors expected — the query is valid, the entity simply does not exist")
                .isNullOrEmpty();
    }

    @Test
    @DisplayName("malformed query returns errors[].message about syntax and no data")
    @Story("Syntax error")
    void malformedQueryShouldReturnSyntaxErrorAndNoData() {
        AssertableGraphQLResponse response = api.graphQL().client()
                .executeGql(MALFORMED_QUERY)
                .then();

        List<String> errorMessages = response.errorMessages();

        assertThat(errorMessages)
                .as("errors array must be present for a syntax error")
                .isNotNull()
                .isNotEmpty();
        assertThat(errorMessages)
                .as("at least one message should point to a syntax problem")
                .anySatisfy(message -> assertThat(message.toLowerCase())
                        .containsAnyOf("syntax", "expected", "parse"));

        response.body("movies", nullValue());
    }

    @Test
    @DisplayName("query on a non-existent field returns a validation error")
    @Story("Validation error")
    void nonExistentFieldShouldReturnValidationError() {
        AssertableGraphQLResponse response = api.graphQL().client()
                .executeGql(NON_EXISTENT_FIELD_QUERY)
                .then();

        List<String> errorMessages = response.errorMessages();

        assertThat(errorMessages)
                .as("errors array must be present for a validation error")
                .isNotNull()
                .isNotEmpty();
        assertThat(errorMessages)
                .as("at least one message should mention the unknown field")
                .anySatisfy(message -> assertThat(message)
                        .containsAnyOf("thisFieldDoesNotExist", "Cannot query field"));

        response.body("movies", nullValue());
    }

}
