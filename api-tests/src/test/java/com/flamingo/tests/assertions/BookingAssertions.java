package com.flamingo.tests.assertions;

import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import org.flamingo.payloads.booking.BookingPayload;
import org.flamingo.responses.rest.booking.BookingByIdResponse;
import org.flamingo.responses.rest.booking.BookingResponse.Booking;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@AllArgsConstructor
public final class BookingAssertions {

    @Step(value = "Assert that booking matches expected booking payload")
    public static void assertThatBookingMatches(Booking actual, BookingPayload expected) {
        assertBookingFields(
                actual.getFirstName(),
                actual.getLastName(),
                actual.getTotalpPrice(),
                actual.getDepositPaid(),
                actual.getAdditionalNeeds(),
                actual.getBookingDates().getCheckIn(),
                actual.getBookingDates().getCheckOut(),
                expected
        );
    }

    @Step(value = "Assert that booking with id matches expected booking payload")
    public static void assertThatBookingMatches(BookingByIdResponse.Booking actual, BookingPayload expected) {
        assertBookingFields(
                actual.getFirstName(),
                actual.getLastName(),
                actual.getTotalpPrice(),
                actual.getDepositPaid(),
                actual.getAdditionalNeeds(),
                actual.getBookingDates().getCheckIn(),
                actual.getBookingDates().getCheckOut(),
                expected
        );
    }

    private static void assertBookingFields(
            String firstName,
            String lastName,
            Integer totalPrice,
            Boolean depositPaid,
            String additionalNeeds,
            String checkin,
            String checkout,
            BookingPayload expected
    ) {
        assertSoftly(softly -> {
            softly.assertThat(expected).as("Expected booking payload").isNotNull();
            softly.assertThat(firstName).as("First name").isEqualTo(expected.getFirstName());
            softly.assertThat(lastName).as("Last name").isEqualTo(expected.getLastName());
            softly.assertThat(totalPrice).as("Total price").isEqualTo(expected.getTotalPrice());
            softly.assertThat(depositPaid).as("Deposit paid").isEqualTo(expected.getDepositPaid());
            softly.assertThat(additionalNeeds).as("Additional needs").isEqualTo(expected.getAdditionalNeeds());
            softly.assertThat(checkin).as("Check-in date").isEqualTo(expected.getBookingDates().getCheckin());
            softly.assertThat(checkout).as("Check-out date").isEqualTo(expected.getBookingDates().getCheckout());
        });
    }
}
