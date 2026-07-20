package com.flamingo.ui;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config.properties"})
public interface UiProjectConfig extends Config {

    @Key("baseUrl")
    String baseUrl();

    @Key("headless")
    boolean headless();

}
