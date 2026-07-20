package org.flamingo.responses.rest.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookingResponse {

    @JsonProperty("booking")
    private Booking booking;

    @JsonProperty("bookingid")
    private int bookingId;


    @Data
    public static class Booking {

        @JsonProperty("firstname")
        private String firstName;

        @JsonProperty("additionalneeds")
        private String additionalNeeds;

        @JsonProperty("bookingdates")
        private BookingDates bookingDates;

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