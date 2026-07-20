package com.flamingo.ui.basic;

import com.flamingo.ui.pages.StudentRegistrationPage;
import com.flamingo.ui.pages.SubmittedFormModal;
import lombok.Getter;

@Getter
public class Application {

    private final StudentRegistrationPage studentRegistrationPage = new StudentRegistrationPage();
    private final SubmittedFormModal submittedForm = new SubmittedFormModal();

}
