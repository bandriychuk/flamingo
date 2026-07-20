package com.flamingo.ui.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatter {

    private static final DateTimeFormatter DOB_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM,yyyy", Locale.ENGLISH);

    public static String formatDob(java.util.Date dob) {
        if (dob == null) return null;

        LocalDate localDate = dob.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate.format(DOB_FORMAT);
    }
}
