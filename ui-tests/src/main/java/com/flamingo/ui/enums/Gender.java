package com.flamingo.ui.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String value;
}
