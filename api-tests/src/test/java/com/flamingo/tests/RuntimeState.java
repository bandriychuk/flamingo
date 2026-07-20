package com.flamingo.tests;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class RuntimeState {

    private final List<Integer> bookings = new CopyOnWriteArrayList<>();

    public void addBooking(int bookingId) {
        this.bookings.add(bookingId);
    }

    public void clear() {
        this.bookings.clear();
    }
}
