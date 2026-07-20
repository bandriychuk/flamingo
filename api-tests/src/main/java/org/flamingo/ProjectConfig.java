package org.flamingo;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config.properties"})
public interface ProjectConfig extends Config {

    @Key("baseUrl")
    String baseUrl();

    @Key("baseGraphQLUrl")
    String baseGraphQLUrl();

    @DefaultValue("en_UK")
    String locale();

    @DefaultValue("true")
    boolean logging();

}
