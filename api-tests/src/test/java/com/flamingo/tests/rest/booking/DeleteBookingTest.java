package com.flamingo.tests.rest.booking;

import com.flamingo.tests.BaseApiTests;
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
@DisplayName("Booking — delete")
public class DeleteBookingTest extends BaseApiTests {

    String accessToken;
    BookingResponse createdBooking;

    @BeforeEach
    public void setup() {
        accessToken = loginAsDefaultUser();
        var bookingPayload = BookingTestData.defaultBooking(faker);
        createdBooking = api.rest().bookingService()
                .createBooking(bookingPayload)
                .shouldHave(Conditions.statusCode(200))
                .asPojo(BookingResponse.class);
    }

    @Tag("regression")
    @Tag("smoke")
    @DisplayName("User can delete booking")
    @Test
    void userCanDeleteBooking() {
        api.rest().bookingService().deleteBookingById(createdBooking.getBookingId(), accessToken)
                .shouldHave(Conditions.statusCode(201));
    }
}
