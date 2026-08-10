package com.example.elhabashyback.listing.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MediaUploadStatus {
    UPLOADING("uploading"),
    PROCESSING("processing"),
    READY("ready"),
    FAILED("failed");

    private final String value;

    MediaUploadStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
