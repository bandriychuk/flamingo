package com.flamingo.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SubmittedFormModal extends PageHolder {

    protected Locator modalValueCell(String label) {
        return page().locator(".table-responsive td", new Page.LocatorOptions().setHasText(label)).locator("xpath=following-sibling::td[1]");
    }

    public Locator successModalTitle() {
        return page().locator("#example-modal-sizes-title-lg");
    }

    public String submittedValue(String label) {
        return modalValueCell(label).textContent().trim();
    }
}
