package com.example.elhabashyback.about.repository;

import com.example.elhabashyback.about.entity.AboutDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AboutDepartmentRepository extends JpaRepository<AboutDepartment, Long> {
    List<AboutDepartment> findAllByOrderByDisplayOrderAscIdAsc();
}
