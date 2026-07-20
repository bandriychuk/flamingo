package com.flamingo.ui.assertions;

import com.flamingo.ui.dto.StudentDto;
import com.flamingo.ui.pages.SubmittedFormModal;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.flamingo.ui.utils.DateFormatter.formatDob;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SubmittedFormAssertions {

    public static void assertThatSubmittedFormMatches(SubmittedFormModal form, StudentDto student) {
        Map<String, String> expected = expectedFromStudent(student);

        assertSoftly(softly -> expected.forEach((label, value) ->
                softly.assertThat(form.submittedValue(label)).as(label).isEqualTo(value)));
    }

    private static Map<String, String> expectedFromStudent(StudentDto s) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Student Name", s.getFirstName() + " " + s.getLastName());
        map.put("Gender", s.getGender().getValue());
        map.put("Mobile", s.getMobile().replaceAll("\\D", ""));

        checkIfPresent(map,
                "Student Email",
                s.getEmail());
        checkIfPresent(map,
                "Subjects",
                s.getSubject());
        checkIfPresent(map,
                "Hobbies",
                s.getHobbies() != null ? s.getHobbies().getValue() : null);
        checkIfPresent(map,
                "Address",
                s.getAddress());
        checkIfPresent(map,
                "Date of Birth",
                formatDob(s.getDob()));
        checkIfPresent(map,
                "Picture",
                s.getPicture() != null ? s.getPicture().toString() : null);
        checkIfPresent(map,
                "State and City",
                s.getState() != null && s.getCity() != null ? s.getState() + " " + s.getCity() : null);

        return map;
    }

    private static void checkIfPresent(Map<String, String> map, String key, String value) {
        if (value != null) map.put(key, value);
    }

}
