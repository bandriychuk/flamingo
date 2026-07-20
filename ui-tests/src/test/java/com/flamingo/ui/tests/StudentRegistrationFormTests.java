package com.flamingo.ui.tests;

import com.flamingo.ui.assertions.SubmittedFormAssertions;
import com.flamingo.ui.dto.StudentDto;
import com.flamingo.ui.enums.Gender;
import com.flamingo.ui.enums.Hobbies;
import com.flamingo.ui.pages.SubmittedFormModal;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("Student UI")
@Feature("Student form")
@DisplayName("Student — registration")
@Severity(SeverityLevel.CRITICAL)
class StudentRegistrationFormTests extends BaseTest {

    @Test
    @DisplayName("User can fill registration form and submit")
    void userCanFillRegistrationFormUploadFileChooseDateAndSubmit() {
        StudentDto student = StudentDto.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .gender(Gender.OTHER)
                .mobile(faker.phoneNumber().subscriberNumber(10))
                .dob(faker.date().birthday(18, 65))
                .hobbies(Hobbies.SPORTS)
                .subject("Maths")
                .build();

        SubmittedFormModal modal = app.getStudentRegistrationPage()
                .open()
                .fillStudentData(student)
                .submit();

        assertThat(modal.successModalTitle().textContent())
                .as("Success modal title")
                .isEqualTo("Thanks for submitting the form");

        SubmittedFormAssertions.assertThatSubmittedFormMatches(modal, student);
    }

    @Test
    @DisplayName("User can fill registration form with picture and submit")
    void userCanFillRegistrationFormUploadFileChooseDateAndSubmitFailedTest() {
        StudentDto student = StudentDto.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .gender(Gender.OTHER)
                .mobile(faker.phoneNumber().subscriberNumber(10))
                .dob(faker.date().birthday(18, 65))
                .hobbies(Hobbies.SPORTS)
                .subject("Maths")
                .picture("src/test/resources/files/student-photo.txt")
                .address(faker.address().fullAddress())
                .state("NCR")
                .city("Delhi")
                .build();

        SubmittedFormModal modal = app.getStudentRegistrationPage()
                .open()
                .fillStudentData(student)
                .submit();

        assertThat(modal.successModalTitle().textContent())
                .as("Success modal title")
                .isEqualTo("Thanks for submitting the form");

        SubmittedFormAssertions.assertThatSubmittedFormMatches(modal, student);
    }
}
