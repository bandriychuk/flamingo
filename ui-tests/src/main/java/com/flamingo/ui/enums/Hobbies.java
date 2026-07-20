package com.flamingo.ui.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Hobbies {

    SPORTS("Sports"),
    READING("Reading"),
    MUSIC("Music");

    private final String value;
}
