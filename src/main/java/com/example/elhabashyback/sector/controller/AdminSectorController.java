package com.example.elhabashyback.sector.controller;

import com.example.elhabashyback.sector.dto.SectorResponse;
import com.example.elhabashyback.sector.dto.UpdateSectorRequest;
import com.example.elhabashyback.sector.service.SectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/sectors")
@RequiredArgsConstructor
public class AdminSectorController {

    private final SectorService sectorService;

    @PatchMapping("/{code}")
    SectorResponse update(@PathVariable String code, @Valid @RequestBody UpdateSectorRequest request) {
        return sectorService.update(code, request);
    }
}
