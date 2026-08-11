package com.example.elhabashyback.about.dto;

import java.util.List;

public record AboutResponse(
        AboutProfileResponse profile,
        List<AboutPersonResponse> people,
        List<AboutDepartmentResponse> departments,
        List<AboutCertificateResponse> certificates,
        List<AboutWorkCategoryResponse> workCategories
) {
}
