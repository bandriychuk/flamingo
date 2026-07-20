package com.flamingo.tests.testdata;

import com.github.javafaker.Faker;
import org.flamingo.payloads.booking.BookingPayload;

import java.time.LocalDate;

public final class BookingTestData {

    private BookingTestData() {
    }

    private static BookingPayload booking(
            Faker faker,
            int totalPrice,
            boolean depositPaid,
            int monthsToAdd,
            String additionalNeeds
    ) {
        return BookingPayload.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .totalPrice(totalPrice)
                .depositPaid(depositPaid)
                .bookingDates(BookingPayload.BookingDates.builder()
                        .checkin(LocalDate.now().plusMonths(monthsToAdd).plusDays(1).toString())
                        .checkout(LocalDate.now().plusMonths(monthsToAdd).plusDays(4).toString())
                        .build())
                .additionalNeeds(additionalNeeds)
                .build();
    }

    public static BookingPayload defaultBooking(Faker faker) {
        return booking(faker, 111, true, 1, "Breakfast");
    }

    public static BookingPayload updatedBooking(Faker faker) {
        return booking(faker, 222, false, 2, "Late checkout");
    }
}
