package com.martin.demo.model;

public enum ScoringAction {

    // Booking
    CREATE_BOOKING(10),
    APPROVE_BOOKING(10),

    // Items / Products
    CREATE_ITEM(50),
    UPLOAD_ITEM_IMAGE(25),

    // Availability
    CREATE_AVAILABILITY(5),
    CREATE_UNAVAILABILITY(5),

    // Events
    CREATE_EVENT(20),
    RSVP_EVENT(0),

    // Friends
    SEND_FRIEND_REQUEST(100),
    ACCEPT_FRIEND_REQUEST(100),

    // Time tracking
    START_WORK(0),
    STOP_WORK(0),

    // Loans
    CREATE_LOAN(0),
    ADD_LOAN_PAYMENT(0),

    // Applications
    CREATE_APPLICATION(0),
    RESPOND_APPLICATION(0),

    // Shopping lists
    CREATE_SHOPPING_LIST(0),
    ADD_SHOPPING_LIST_ITEM(0),
    TOGGLE_ITEM_BOUGHT(0);

    private final int points;

    ScoringAction(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
