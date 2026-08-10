package com.example.elhabashyback.settings.repository;

import com.example.elhabashyback.settings.entity.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Short> {
}
