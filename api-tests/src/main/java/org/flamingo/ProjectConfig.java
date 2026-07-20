package org.flamingo;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config.properties"})
public interface ProjectConfig extends Config {

    @Key("baseUrl")
    String baseUrl();

    @Key("baseGraphQLUrl")
    String baseGraphQLUrl();

    @Key("admin.username")
    @DefaultValue("admin")
    String adminUsername();

    @Key("admin.password")
    @DefaultValue("password123")
    String adminPassword();

    @DefaultValue("true")
    boolean logging();

}
