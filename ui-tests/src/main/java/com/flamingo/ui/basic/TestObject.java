package com.flamingo.ui.basic;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public record TestObject(Playwright playwright, Browser browser, BrowserContext browserContext, Page page) {
}
