package com.flamingo.ui.basic;


import com.flamingo.ui.UiProjectConfig;
import com.microsoft.playwright.*;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaywrightContainer {

    public static final UiProjectConfig config = ConfigFactory.create(UiProjectConfig.class, System.getProperties());

    private static final Path ARTIFACTS_DIR = Paths.get("build", "playwright-artifacts/videos/");

    public static ThreadLocal<TestObject> testObjectStorage = new ThreadLocal<>();

    public static TestObject testObject() {
        TestObject testObject = testObjectStorage.get();
        if (testObject != null) {
            return testObject;
        }
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(config.headless());

        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setBaseURL(config.baseUrl())
                .setRecordVideoDir(Path.of(ARTIFACTS_DIR.toString()));

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(launchOptions);
        BrowserContext browserContext = browser.newContext(options);

        browserContext
                .tracing()
                .start(new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true));

        Page page = browserContext.newPage();
        testObject = new TestObject(playwright, browser, browserContext, page);

        testObjectStorage.set(testObject);

        return testObjectStorage.get();
    }
}

