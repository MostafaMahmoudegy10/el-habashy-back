package com.example.elhabashyback.sector.controller;

import com.example.elhabashyback.sector.dto.SectorResponse;
import com.example.elhabashyback.sector.service.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/sectors")
@RequiredArgsConstructor
public class PublicSectorController {

    private final SectorService sectorService;

    @GetMapping
    List<SectorResponse> list() {
        return sectorService.list();
    }
}
