package org.flamingo.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GraphQLQuery {

    private final String query;
    private Object variables;
}
