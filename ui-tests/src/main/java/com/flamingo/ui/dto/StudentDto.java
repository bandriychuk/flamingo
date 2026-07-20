package com.flamingo.ui.dto;

import com.flamingo.ui.enums.Gender;
import com.flamingo.ui.enums.Hobbies;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class StudentDto {

    @NonNull private String firstName;
    @NonNull private String lastName;
    private String email;
    @NonNull private Gender gender;
    @NonNull private String mobile;
    private Date dob;
    private String subject;
    private Hobbies hobbies;
    private String picture;
    private String address;
    private String state;
    private String city;

}