package com.example.elhabashyback.expertise.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ServiceKind {
    ARBITRATION("arbitration"),
    VALUATION("valuation"),
    CONSULTING("consulting");

    private final String value;

    ServiceKind(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static ServiceKind fromValue(String value) {
        return Arrays.stream(values())
                .filter(kind -> kind.value.equalsIgnoreCase(value) || kind.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported service kind: " + value));
    }
}
