package com.flamingo.tests.rest.booking;

import com.flamingo.tests.assertions.BookingAssertions;
import com.flamingo.tests.testData.BookingTestData;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.flamingo.conditions.Conditions;
import org.flamingo.payloads.booking.BookingPayload;
import org.flamingo.responses.rest.booking.BookingByIdResponse;
import org.flamingo.responses.rest.booking.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("rest")
@Tag("booking")
@Epic("REST API")
@Feature("Booking")
@DisplayName("Booking — read")
public class GetBookingTest extends BookingBaseTest {

    private BookingResponse createdBooking;
    private BookingPayload bookingPayload;

    @BeforeEach
    public void setup() {
        bookingPayload = BookingTestData.defaultBooking(faker);
        createdBooking = this.createdBooking(bookingPayload);
    }

    @Test
    @DisplayName("User can get created booking")
    void userCanGetCreatedBookingById() {
        var actualBooking = api.rest().bookingService()
                .getBookingById(createdBooking.getBookingId())
                .shouldHave(Conditions.statusCode(200))
                .asPojo(BookingByIdResponse.Booking.class);

        BookingAssertions.assertThatBookingMatches(actualBooking, bookingPayload);

    }

}
