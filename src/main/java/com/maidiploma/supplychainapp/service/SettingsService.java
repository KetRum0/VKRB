package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Settings;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.model.WarehousesProducts;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public LocalDate getStart() {
        return settingsRepository.getStartDateById(1L);
    }

    public LocalDate getEnd() {
        return settingsRepository.getCurDateById(1L);
    }

    public int getR() {
        return settingsRepository.getRById(1L);
    }

    public void setStart(LocalDate start) {
        Settings settings = settingsRepository.findById(1L).orElseThrow();
        settings.setStart_date(start);
        settingsRepository.save(settings);
    }

    public void setEnd(LocalDate end) {
        Settings settings = settingsRepository.findById(1L).orElseThrow();
        settings.setCur_date(end);
        settingsRepository.save(settings);
    }

    public void setR(int r) {
        Settings settings = settingsRepository.findById(1L).orElseThrow();
        settings.setR(r);
        settingsRepository.save(settings);
    }

    public void initSettings(LocalDate cur_date, LocalDate start_date, int r) {
        Settings settings = new Settings();
        settings.setStart_date(start_date);
        settings.setCur_date(cur_date);
        settings.setR(r);
        settings.setId(1L);
        settingsRepository.save(settings);
    }

    public boolean hasSettings() {
        return settingsRepository.count() > 0;
    }
}
