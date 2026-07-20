package com.flamingo.ui.pages;

import com.microsoft.playwright.Page;
import lombok.Getter;

import java.util.function.Consumer;

import static com.flamingo.ui.basic.PlaywrightContainer.testObject;

@Getter
public abstract class PageHolder {

    Page page() {
        return testObject().page();
    }

    protected void clickDropdownOption(String containerId, String optionText) {
        this.page().locator("#" + containerId).click();
        this.page().locator("#" + containerId + " input").fill(optionText);
        this.page().locator("#" + containerId + " input").press("Enter");
    }

    protected static <T> void applyIfPresent(T value, Consumer<T> action) {
        if (value != null) action.accept(value);
    }
}
