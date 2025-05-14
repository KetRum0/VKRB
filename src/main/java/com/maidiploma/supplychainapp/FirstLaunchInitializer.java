package com.maidiploma.supplychainapp;

import com.maidiploma.supplychainapp.service.SettingsService;
import com.maidiploma.supplychainapp.service.WarehousesProductsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FirstLaunchInitializer implements CommandLineRunner {

    private final SettingsService settingsService;
    private final WarehousesProductsService warehousesProductsService;

    public FirstLaunchInitializer(SettingsService settingsService, WarehousesProductsService warehousesProductsService1) {
        this.settingsService = settingsService;
        this.warehousesProductsService = warehousesProductsService1;
    }

    @Override
    public void run(String... args) {
        if (!settingsService.hasSettings()) {
            settingsService.initSettings(LocalDate.now(), LocalDate.now().minusMonths(1), 7);
//            LocalDate start = settingsService.getStart();
//            LocalDate end = settingsService.getEnd();
//            warehousesProductsService.fillZeroStock(start, end);
            System.out.println("Initial settings created.");
        } else {
            System.out.println("Settings already exist. Skipping initialization.");
        }
    }
}
