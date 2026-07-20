package com.flamingo.tests.rest.booking;

import com.flamingo.tests.assertions.BookingAssertions;
import com.flamingo.tests.testData.BookingTestData;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.flamingo.conditions.Conditions;
import org.flamingo.responses.rest.booking.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("rest")
@Tag("booking")
@Epic("REST API")
@Feature("Booking")
@DisplayName("Booking — update")
public class UpdateBookingTest extends BookingBaseTest {

    private String accessToken;
    private int bookingId;

    @BeforeEach
    public void setup() {
        accessToken = loginAsDefaultUser();
        var createdBooking = this.createdBooking(BookingTestData.defaultBooking(faker));
        bookingId = createdBooking.getBookingId();
    }

    @Tag("regression")
    @Test
    @DisplayName("User can update created booking")
    void userCanUpdateCreatedBooking() {
        var updatedBookingPayload = BookingTestData.updatedBooking(faker);

        var updatedBooking = api.rest().bookingService()
                .updateBookingById(bookingId, updatedBookingPayload, accessToken)
                .shouldHave(Conditions.statusCode(200))
                .asPojo(BookingResponse.Booking.class);

        BookingAssertions.assertThatBookingMatches(updatedBooking, updatedBookingPayload);
    }
}
