package org.flamingo.responses.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movies {

    private Data data;
    private Extensions extensions;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private List<Movie> movies;

    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Movie {

        private String id;
        private String title;
        private MoviePoster moviePoster;
        private User publishedBy;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MoviePoster {

        private String url;

    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String id;
        private String name;

    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extensions {

        @JsonProperty("Complexity-Cost-Left")
        private long complexityCostLeft;

        @JsonProperty("Effective-Complexity-Limit")
        private long effectiveComplexityLimit;

    }
}
