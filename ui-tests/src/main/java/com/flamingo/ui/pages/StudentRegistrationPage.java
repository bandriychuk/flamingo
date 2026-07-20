package com.flamingo.ui.pages;

import com.flamingo.ui.dto.StudentDto;
import com.flamingo.ui.enums.Gender;
import com.flamingo.ui.enums.Hobbies;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class StudentRegistrationPage extends PageHolder {

    public StudentRegistrationPage open() {
        this.page().navigate("/automation-practice-form");
        this.page().getByText("Student Registration Form").waitFor();
        return this;
    }

    @Step("Fill student information: {0.firstName} {0.lastName} {0.mobile}")
    public StudentRegistrationPage fillStudentData(StudentDto student) {
        this.fillFirstName(student.getFirstName());
        this.fillLastName(student.getLastName());
        applyIfPresent(student.getGender(), this::selectGender);
        applyIfPresent(student.getMobile(), this::fillMobile);
        fillDateOfBirthIfComplete(student);
        applyIfPresent(student.getEmail(), this::fillEmail);
        applyIfPresent(student.getSubject(), this::fillSubject);
        applyIfPresent(student.getHobbies(), this::selectHobby);
        applyIfPresent(student.getPicture(), s -> uploadPicture(Path.of(s)));
        applyIfPresent(student.getAddress(), this::fillCurrentAddress);
        applyIfPresent(student.getState(), this::selectState);
        applyIfPresent(student.getCity(), this::selectCity);
        return this;
    }

    public SubmittedFormModal submit() {
        this.page().locator("#submit").click();
        return new SubmittedFormModal();
    }

    @Step("Fill {0} field with {1}")
    private void fillFirstName(String firstName) {
        this.page().locator("#firstName").fill(firstName);
    }

    @Step("Fill {0} field with {1}")
    private void fillLastName(String lastName) {
        this.page().locator("#lastName").fill(lastName);
    }

    @Step("Fill {0} field with {1}")
    private void fillEmail(String email) {
        this.page().locator("#userEmail").fill(email);
    }

    @Step("Fill {0} field with {1}")
    private void selectGender(Gender gender) {
        this.page().getByRole(
                AriaRole.RADIO,
                new Page.GetByRoleOptions().setName(gender.getValue()).setExact(true)
        ).check();
    }

    private void fillMobile(String mobile) {
        this.page().locator("#userNumber")
                .fill(mobile.replaceAll("\\D", ""));
    }

    private void selectDateOfBirth(String year, String month, String dayOfMonth) {
        this.page().locator("#dateOfBirthInput").click();
        this.page().locator(".react-datepicker__year-select").selectOption(year);
        this.page().locator(".react-datepicker__month-select").selectOption(month);
        this.page().locator(".react-datepicker__day--0" + dayOfMonth + ":not(.react-datepicker__day--outside-month)").first().click();

    }

    private void fillSubject(String subject) {
        this.page().locator("#subjectsInput").fill(subject);
        this.page().locator(".subjects-auto-complete__menu").getByText(subject).click();
    }

    private void selectHobby(Hobbies hobby) {
        this.page().getByRole(
                AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName(hobby.getValue()).setExact(true)
        ).check();
    }

    private void uploadPicture(Path filePath) {
        this.page().locator("#uploadPicture").setInputFiles(filePath);
    }

    private void fillCurrentAddress(String currentAddress) {
        this.page().locator("#currentAddress").fill(currentAddress);
    }

    private void selectState(String state) {
        clickDropdownOption("state", state);
    }

    private void selectCity(String city) {
        clickDropdownOption("city", city);
    }

    private void fillDateOfBirthIfComplete(StudentDto s) {
        if (s.getDob() == null) {
            return;
        }

        LocalDate birthDate = s.getDob().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String day = String.format("%02d", birthDate.getDayOfMonth());
        String month = birthDate.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String year = String.valueOf(birthDate.getYear());

        selectDateOfBirth(year, month, day);
    }

}
