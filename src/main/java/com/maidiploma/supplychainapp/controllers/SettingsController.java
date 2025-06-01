package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.Settings;
import com.maidiploma.supplychainapp.repository.SettingsRepository;
import com.maidiploma.supplychainapp.service.ProductService;
import com.maidiploma.supplychainapp.service.WarehouseService;
import com.maidiploma.supplychainapp.service.WarehousesProductsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final WarehousesProductsService warehousesProductsService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public SettingsController(SettingsRepository settingsRepository, WarehousesProductsService warehousesProductsService, WarehouseService warehouseService, ProductService productService) {
        this.settingsRepository = settingsRepository;
        this.warehousesProductsService = warehousesProductsService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @GetMapping
    public String getSettings(Model model) {
        Settings settings = settingsRepository.findById(1L).orElse(new Settings(1L, LocalDate.now(), LocalDate.now(), 0));
        model.addAttribute("settings", settings);
        return "settings";
    }

    @PostMapping
    public String updateSettings(@ModelAttribute Settings settings) {
        settings.setId(1L); // всегда 1 запись
        LocalDate now = settingsRepository.findById(1L).get().getCur_date();
        settingsRepository.save(settings);
        warehousesProductsService.fillPreviousStock(now, settings.getCur_date(), warehouseService.getAll(), productService.getAll());
        warehousesProductsService.fillZeroStock(settings.getStart_date(),settings.getCur_date(), warehouseService.getAll(), productService.getAll());

        return "redirect:/settings";
    }
}