package org.flamingo.services.rest;

import io.qameta.allure.Step;
import org.flamingo.assertions.rest.AssertableResponse;
import org.flamingo.clients.HttpSpecs;
import org.flamingo.payloads.booking.BookingPayload;

public class BookingService extends BaseRestService {

    private static final String BOOKING_PATH = "/booking";
    private static final String BOOKING_BY_ID_PATH = BOOKING_PATH + "/{bookingId}";

    public BookingService(HttpSpecs httpSpecs) {
        super(httpSpecs);
    }

    @Step(value = "Create a new booking")
    public AssertableResponse createBooking(BookingPayload bookingPayload) {
        return new AssertableResponse(
                setUp()
                        .when()
                        .body(bookingPayload)
                        .post(BOOKING_PATH));
    }

    @Step(value = "Get booking by id: '{0}'")
    public AssertableResponse getBookingById(int bookingId) {
        return new AssertableResponse(
                setUp()
                        .pathParam("bookingId", bookingId)
                        .when()
                        .get(BOOKING_BY_ID_PATH));
    }

    @Step(value = "Update booking by id: '{0}'")
    public AssertableResponse updateBookingById(int bookingId, BookingPayload bookingPayload, String token) {
        return new AssertableResponse(
                setUpWithAuth(token)
                        .pathParam("bookingId", bookingId)
                        .body(bookingPayload)
                        .put(BOOKING_BY_ID_PATH));
    }

    @Step(value = "Delete booking by id: '{0}'")
    public AssertableResponse deleteBookingById(int bookingId, String token) {
        return new AssertableResponse(
                setUpWithAuth(token)
                        .pathParam("bookingId", bookingId)
                        .delete(BOOKING_BY_ID_PATH));
    }
}
