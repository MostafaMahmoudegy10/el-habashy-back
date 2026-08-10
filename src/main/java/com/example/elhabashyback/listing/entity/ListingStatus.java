package com.example.elhabashyback.listing.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ListingStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    CLOSED("closed"),
    COMING_SOON("coming-soon");

    private final String value;

    ListingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static ListingStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported listing status: " + value));
    }
}
