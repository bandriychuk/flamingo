package org.flamingo.utils.rest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BookingCleanupRegistry {

    private static final List<Integer> IDS = new CopyOnWriteArrayList<>();

    private BookingCleanupRegistry() {}

    public static int register(int bookingId) {
        IDS.add(bookingId);
        return bookingId;
    }

    public static List<Integer> snapshot() {
        return List.copyOf(IDS);
    }

    public static void clear() {
        IDS.clear();
    }
}