package org.flamingo.services;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.aeonbits.owner.ConfigFactory;
import org.flamingo.ProjectConfig;
import org.flamingo.clients.HttpSpecs;


@Accessors(fluent = true)
@Getter
public class ApiServices {

    private final RestServices rest;
    private final GraphQLServices graphQL;

    public ApiServices(String graphQLUrl) {
        var httpSpecs = new HttpSpecs(loadConfig().logging());
        this.rest = new RestServices(httpSpecs);
        this.graphQL = new GraphQLServices(graphQLUrl, httpSpecs);
    }

    private static ProjectConfig loadConfig() {
        return ConfigFactory.create(ProjectConfig.class, System.getProperties());
    }
}
