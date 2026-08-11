package com.example.elhabashyback.sector.service;

import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.configuration.cache.CacheConfiguration;
import com.example.elhabashyback.sector.dto.SectorResponse;
import com.example.elhabashyback.sector.dto.UpdateSectorRequest;
import com.example.elhabashyback.sector.entity.Sector;
import com.example.elhabashyback.sector.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfiguration.PUBLIC_SECTORS, sync = true)
    public List<SectorResponse> list() {
        return sectorRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(SectorResponse::from)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_SECTORS, allEntries = true)
    public SectorResponse update(String code, UpdateSectorRequest request) {
        Sector sector = sectorRepository.findById(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found"));

        sector.setTitleAr(request.title().ar().trim());
        sector.setTitleEn(request.title().en().trim());
        sector.setDescriptionAr(request.description().ar().trim());
        sector.setDescriptionEn(request.description().en().trim());

        sectorRepository.flush();
        return SectorResponse.from(sector);
    }

    private String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }
}
