package com.example.elhabashyback.about.repository;

import com.example.elhabashyback.about.entity.AboutWorkCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AboutWorkCategoryRepository extends JpaRepository<AboutWorkCategory, Long> {
    List<AboutWorkCategory> findAllByOrderByDisplayOrderAscIdAsc();
}
