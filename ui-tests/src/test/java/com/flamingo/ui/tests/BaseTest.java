package com.flamingo.ui.tests;

import com.flamingo.ui.basic.Application;
import com.flamingo.ui.basic.PlaywrightContainer;
import com.flamingo.ui.basic.TestObject;
import com.github.javafaker.Faker;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import io.qameta.allure.Allure;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTest {

    protected Application app = new Application();
    protected Faker faker = new Faker();

    private boolean testFailed = false;

    @RegisterExtension
    final AfterTestExecutionCallback captureFailure = ctx ->
            testFailed = ctx.getExecutionException().isPresent();

    @AfterEach
    @SneakyThrows
    void attachArtifactsAndClose(TestInfo info) {
        TestObject testObject = PlaywrightContainer.testObjectStorage.get();
        if (testObject == null) return;

        Path trace = Paths.get("build/playwright/traces", info.getDisplayName() + ".zip");

        if (testFailed) {
            Files.createDirectories(trace.getParent());

            Allure.addAttachment("Screenshot", "image/png",
                    new ByteArrayInputStream(testObject.page().screenshot()), "png");

            testObject.browserContext().tracing().stop(new Tracing.StopOptions().setPath(trace));
            Allure.addAttachment("Trace", "application/zip",
                    new ByteArrayInputStream(Files.readAllBytes(trace)), "zip");
        }

        Video video = testObject.page().video();
        testObject.browserContext().close();

        if (testFailed && video != null) {
            Allure.addAttachment("Video", "video/webm",
                    new ByteArrayInputStream(Files.readAllBytes(video.path())), "webm");
        }

        PlaywrightContainer.testObjectStorage.remove();
        testFailed = false;
    }
}
