package com.example.elhabashyback.about.repository;

import com.example.elhabashyback.about.entity.AboutPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AboutPersonRepository extends JpaRepository<AboutPerson, Long> {
    List<AboutPerson> findAllByOrderByDisplayOrderAscIdAsc();
    List<AboutPerson> findByActiveTrueOrderByDisplayOrderAscIdAsc();
}
