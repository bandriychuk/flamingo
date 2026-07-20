package org.flamingo.responses.rest.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookingByIdResponse {

    @Data
    public static class Booking {

        @JsonProperty("firstname")
        private String firstName;

        @JsonProperty("additionalneeds")
        private String additionalNeeds;

        @JsonProperty("bookingdates")
        private BookingResponse.Booking.BookingDates bookingDates;

        @JsonProperty("totalprice")
        private Integer totalpPrice;

        @JsonProperty("depositpaid")
        private Boolean depositPaid;

        @JsonProperty("lastname")
        private String lastName;

        @Data
        public static class BookingDates {

            @JsonProperty("checkin")
            private String checkIn;

            @JsonProperty("checkout")
            private String checkOut;
        }
    }

}
