package com.example.elhabashyback.about.repository;

import com.example.elhabashyback.about.entity.AboutWorkEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AboutWorkEntryRepository extends JpaRepository<AboutWorkEntry, Long> {
    List<AboutWorkEntry> findAllByOrderByCategoryDisplayOrderAscDisplayOrderAscIdAsc();
    List<AboutWorkEntry> findByCategoryIdOrderByDisplayOrderAscIdAsc(Long categoryId);

    @Modifying
    @Query("delete from AboutWorkEntry entry where entry.category.id = :categoryId")
    int deleteAllByCategoryId(@Param("categoryId") Long categoryId);
}
