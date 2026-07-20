package org.flamingo.payloads.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookingPayload {

    @JsonProperty("firstname")
    private String firstName;

    @JsonProperty("additionalneeds")
    private String additionalNeeds;

    @JsonProperty("bookingdates")
    private BookingDates bookingDates;

    @JsonProperty("totalprice")
    private Integer totalPrice;

    @JsonProperty("depositpaid")
    private Boolean depositPaid;

    @JsonProperty("lastname")
    private String lastName;

	@Builder
    @Data
    public static class BookingDates {

        @JsonProperty("checkin")
        private String checkin;

        @JsonProperty("checkout")
        private String checkout;
    }
}