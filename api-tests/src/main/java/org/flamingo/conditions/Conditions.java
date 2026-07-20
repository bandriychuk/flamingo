package org.flamingo.conditions;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;

@UtilityClass
public class Conditions {

    public StatusCodeCondition statusCode(int statusCode) {
        return new StatusCodeCondition(statusCode);
    }

    public BodyFieldCondition bodyField(String jsonPath, Matcher matcher) {
        return new BodyFieldCondition(jsonPath, matcher);
    }
}
