package com.flamingo.tests.rest.booking;

import com.flamingo.tests.BaseApiTests;
import lombok.extern.slf4j.Slf4j;
import org.flamingo.conditions.Conditions;
import org.flamingo.payloads.booking.BookingPayload;
import org.flamingo.responses.rest.booking.BookingResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BookingBaseTest extends BaseApiTests {

    public BookingResponse createdBooking(BookingPayload bookingPayload){
        var createdBooking = api.rest().bookingService()
                .createBooking(bookingPayload)
                .shouldHave(Conditions.statusCode(200))
                .shouldHave(Conditions.bodyField("bookingid", notNullValue()))
                .asPojo(BookingResponse.class);

        runtimeState.addBooking(createdBooking.getBookingId());

        return createdBooking;
    }


    @AfterAll
    void cleanUpTestData() {
        List<Integer> bookings = runtimeState.getBookings();
        if (!bookings.isEmpty()){
            bookings.forEach(booking -> {
                log.info("Deleting booking with id: " + booking + "");
                api.rest().bookingService().deleteBookingById(booking, this.loginAsDefaultUser());
             }
            );
        }
    }


}
