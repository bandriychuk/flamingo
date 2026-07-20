package org.flamingo.utils.graphql;

import org.flamingo.clients.GraphQLQuery;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

public class GqlUtils {

    public static GraphQLQuery readGql(String name) {
        return gql(readFile(name));
    }

    public static GraphQLQuery readGql(String name, Object variables) {
        return gql(readFile(name), variables);
    }

    private static String readFile(String name) {
        URL url = GqlUtils.class
                .getClassLoader()
                .getResource(name);

        File file = new File(Objects.requireNonNull(url).getFile());
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read " + name, e);
        }
    }

    public static GraphQLQuery gql(String queryString) {
        return new GraphQLQuery(queryString);
    }

    public static GraphQLQuery gql(String queryString, Object variables) {
        return new GraphQLQuery(queryString, variables);
    }
}
