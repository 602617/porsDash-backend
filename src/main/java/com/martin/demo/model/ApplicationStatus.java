package com.martin.demo.model;

public enum ApplicationStatus {
    PENDING,        // Waiting for receiver response
    COUNTERED,      // Counter-offer made, waiting for response
    ACCEPTED,
    DECLINED
}
