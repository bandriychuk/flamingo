package com.flamingo.ui.tests;

import com.flamingo.ui.assertions.SubmittedFormAssertions;
import com.flamingo.ui.dto.StudentDto;
import com.flamingo.ui.enums.Gender;
import com.flamingo.ui.enums.Hobbies;
import com.flamingo.ui.pages.SubmittedFormModal;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("Student UI")
@Feature("Student form")
@DisplayName("Student Parametrized tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParametrizedStudentRegistrationFormTests extends BaseTest {

    @Tag("regression")
    @ParameterizedTest(name = "{index} — {0}")
    @MethodSource("studentRegistrationData")
    @DisplayName("User can fill registration form and submit")
    void userCanFillRegistrationFormAndSubmit(String testName, StudentDto student) {
        SubmittedFormModal modal = app.getStudentRegistrationPage()
                .open()
                .fillStudentData(student)
                .submit();

        assertThat(modal.successModalTitle().textContent())
                .as("Success modal title")
                .isEqualTo("Thanks for submitting the form");

        SubmittedFormAssertions.assertThatSubmittedFormMatches(modal, student);
    }

    private Stream<Arguments> studentRegistrationData() {
        return Stream.of(
                Arguments.of(
                        "Required fields only",
                        StudentDto.builder()
                                .firstName(faker.name().firstName())
                                .lastName(faker.name().lastName())
                                .email(faker.internet().emailAddress())
                                .gender(Gender.OTHER)
                                .mobile(faker.phoneNumber().subscriberNumber(10))
                                .dob(faker.date().birthday(18, 65))
                                .hobbies(Hobbies.SPORTS)
                                .subject("Maths")
                                .build()
                ),
                Arguments.of(
                        "All fields",
                        StudentDto.builder()
                                .firstName(faker.name().firstName())
                                .lastName(faker.name().lastName())
                                .email(faker.internet().emailAddress())
                                .gender(Gender.FEMALE)
                                .mobile(faker.phoneNumber().subscriberNumber(10))
                                .dob(faker.date().birthday(18, 65))
                                .hobbies(Hobbies.MUSIC)
                                .subject("Maths")
                                .picture("src/test/resources/files/student-photo.txt")
                                .address(faker.address().fullAddress())
                                .state("NCR")
                                .city("Delhi")
                                .build()
                )
        );
    }
}