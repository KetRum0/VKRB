package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ForecastController {

    private final ProductService productService;
    private final ForecastService forecastService;
    private final SettingsService settingsService;
    private final WarehouseService warehouseService;

    public ForecastController(
            ProductService productService,
            ForecastService forecastService,
            SettingsService settingsService,
            WarehouseService warehouseService
    ) {
        this.productService = productService;
        this.forecastService = forecastService;
        this.settingsService = settingsService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/forecast")
    public String getForecastPage(Model model) {
        List<Product> products = productService.getAll();
        List<Warehouse> warehouses = warehouseService.getAll();
        HistoricalData historicalData = forecastService.getHistoricalDemand(8L, 6L, settingsService.getStart(), settingsService.getEnd()); //change

        model.addAttribute("products", products);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("historical", historicalData.getQuantities());
        model.addAttribute("days", historicalData.getDates());

        return "forecast";
    }

    @PostMapping("/forecast/run")
    public String calculateForecast(
            @RequestParam("days") int days,
            @RequestParam("productId") Long productId,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam Map<String, String> allParams,
            Model model) {

        if (allParams.containsKey("movingAverage")) {
            Integer period = parseIntOrNull(allParams.get("movingAverage_period"));
            HistoricalData forecasrted1 = forecastService.MA(productId, warehouseId, days, period);
            model.addAttribute("days", forecasrted1.getDates());
            model.addAttribute("forecasted1", forecasrted1.getQuantities());

        }

        if (allParams.containsKey("expSmooth1")) {
            Double alpha = parseDoubleOrNull(allParams.get("expSmooth1_alpha"));
            HistoricalData forecasrted2 = forecastService.SES(productId, warehouseId, days, alpha);
            model.addAttribute("days", forecasrted2.getDates());
            model.addAttribute("forecasted2", forecasrted2.getQuantities());
        }

        if (allParams.containsKey("expSmooth2")) {
            Double alpha = parseDoubleOrNull(allParams.get("expSmooth2_alpha"));
            Double beta = parseDoubleOrNull(allParams.get("expSmooth2_b"));
            HistoricalData forecasrted3 = forecastService.DES(productId, warehouseId, days, alpha, beta);
            model.addAttribute("days", forecasrted3.getDates());
            model.addAttribute("forecasted3", forecasrted3.getQuantities());        }

        if (allParams.containsKey("expSmooth3")) {
            Double alpha = parseDoubleOrNull(allParams.get("expSmooth3_alpha"));
            Double beta = parseDoubleOrNull(allParams.get("expSmooth3_b"));
            Double y = parseDoubleOrNull(allParams.get("expSmooth3_y"));
            HistoricalData forecasrted4 = forecastService.TES(productId, warehouseId, days, alpha, beta, y);
            model.addAttribute("days", forecasrted4.getDates());
            model.addAttribute("forecasted4", forecasrted4.getQuantities());}


        model.addAttribute("message", "Расчёт прогноза успешно завершён!");
        List<Product> products = productService.getAll();
        List<Warehouse> warehouses = warehouseService.getAll();

        HistoricalData historicalData = forecastService.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());

        model.addAttribute("products", products);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("historical", historicalData.getQuantities());

        return "forecast";
    }

    private Integer parseIntOrNull(String value) {
        try {
            return (value != null && !value.isBlank()) ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleOrNull(String value) {
        try {
            return (value != null && !value.isBlank()) ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}