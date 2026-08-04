package com.example.elhabashyback.sector.repository;

import com.example.elhabashyback.sector.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, String> {

    List<Sector> findAllByOrderByDisplayOrderAsc();
}
