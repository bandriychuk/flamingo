package org.flamingo.services;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.flamingo.clients.HttpSpecs;
import org.flamingo.services.rest.AuthService;
import org.flamingo.services.rest.BookingService;

@Accessors(fluent = true)
@Getter
public class RestServices {

    private final AuthService authService;
    private final BookingService bookingService;

    public RestServices(HttpSpecs httpSpecs) {
        this.authService = new AuthService(httpSpecs);
        this.bookingService = new BookingService(httpSpecs);
    }
}
