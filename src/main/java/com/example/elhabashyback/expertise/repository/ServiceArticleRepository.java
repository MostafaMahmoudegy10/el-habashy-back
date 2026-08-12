package com.example.elhabashyback.expertise.repository;

import com.example.elhabashyback.expertise.entity.ServiceArticle;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceArticleRepository extends JpaRepository<ServiceArticle, Long> {

    List<ServiceArticle> findAllByOrderByDisplayOrderAscIdAsc();

    List<ServiceArticle> findByKindOrderByDisplayOrderAscIdAsc(ServiceKind kind);

    Optional<ServiceArticle> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);
}
