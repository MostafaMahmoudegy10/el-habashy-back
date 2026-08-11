package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutProfile;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;

public record AboutProfileResponse(
        LocalizedTextResponse headline,
        LocalizedTextResponse profile,
        LocalizedTextResponse mission,
        LocalizedTextResponse vision,
        String imageUrl,
        Integer startedYear,
        Instant updatedAt
) {
    public static AboutProfileResponse from(AboutProfile profile) {
        return new AboutProfileResponse(
                new LocalizedTextResponse(profile.getHeadlineAr(), profile.getHeadlineEn()),
                new LocalizedTextResponse(profile.getProfileAr(), profile.getProfileEn()),
                new LocalizedTextResponse(profile.getMissionAr(), profile.getMissionEn()),
                new LocalizedTextResponse(profile.getVisionAr(), profile.getVisionEn()),
                profile.getProfileImageUrl(),
                profile.getStartedYear(),
                profile.getUpdatedAt()
        );
    }
}
