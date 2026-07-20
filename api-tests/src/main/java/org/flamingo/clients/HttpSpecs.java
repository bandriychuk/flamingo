package org.flamingo.clients;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;

public final class HttpSpecs {

    private final boolean logHttp;

    public HttpSpecs(boolean logHttp) {
        this.logHttp = logHttp;
    }

    public RequestSpecification jsonSpec() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .filters(filters());
    }

    private List<Filter> filters() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new AllureRestAssured());
        if (logHttp) {
            filters.add(new RequestLoggingFilter());
            filters.add(new ResponseLoggingFilter());
        }
        return filters;
    }
}