package com.example.elhabashyback.about.repository;

import com.example.elhabashyback.about.entity.AboutCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AboutCertificateRepository extends JpaRepository<AboutCertificate, Long> {
    List<AboutCertificate> findAllByOrderByDisplayOrderAscIdAsc();
}
