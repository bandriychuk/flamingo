package com.flamingo.tests.rest.booking;

import com.flamingo.tests.assertions.BookingAssertions;
import com.flamingo.tests.testData.BookingTestData;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("rest")
@Tag("booking")
@Epic("REST API")
@Feature("Booking")
@DisplayName("Booking — create")
@Severity(SeverityLevel.CRITICAL)
public class CreateBookingTest extends BookingBaseTest {

    @Test
    @DisplayName("User can create booking")
    void userCanCreateBooking() {

        var bookingPayload = BookingTestData.defaultBooking(faker);
        var booking = this.createdBooking(bookingPayload);

        BookingAssertions.assertThatBookingMatches(booking.getBooking(), bookingPayload);
    }
}
