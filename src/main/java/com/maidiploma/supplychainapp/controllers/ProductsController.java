package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.repository.*;
import com.maidiploma.supplychainapp.service.ProductService;
import com.maidiploma.supplychainapp.service.SettingsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private WarehousesProductsRepository warehousesProductsRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private ShipmentsProductsRepository shipmentsProductsRepository;
    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAll();
        model.addAttribute("products", products);
        return "products";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product) {
        System.out.println(product.getPrice());
        productService.save(product);
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        productService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file) {
        productService.importExcel(file);
        return "redirect:/products";

    }

    @PostMapping("demand/import")
    public String importDemandFromExcel(@RequestParam("file") MultipartFile file, Long productId, Long warehouseId) throws IOException {
        productService.importDemandFromExcel(file, productId, warehouseId);
        return "redirect:/products/view/" + productId;

    }

    @GetMapping("/view/{id}")
    public String getProductStats(@PathVariable Long id, Model model) {
        LocalDate start_date = settingsService.getStart();
        LocalDate end_date = settingsService.getEnd();

        List<Object[]> stockData = warehousesProductsRepository.findByProductIdAndDate(id, end_date);
        List<String> stockLabels = new ArrayList<>();
        List<Integer> stockValues = new ArrayList<>();
        for (Object[] row : stockData) {
            stockLabels.add((String) row[0]);
            stockValues.add(((Number) row[1]).intValue());
        }

        List<Object[]> demandData = shipmentsProductsRepository.findTotalProductShippedByWarehouseName(id);
        List<String> demandLabels = new ArrayList<>();
        List<Integer> demandValues = new ArrayList<>();
        for (Object[] row : demandData) {
            demandLabels.add((String) row[0]);
            demandValues.add(((Number) row[1]).intValue());
        }

        List<Warehouse> allWarehouses = warehouseRepository.findAll();
        Map<Long, List<String>> dateLabelsByWarehouse = new HashMap<>();
        Map<Long, List<Integer>> demandDataByWarehouse = new HashMap<>();

        for (Warehouse warehouse : allWarehouses) {
            List<Object[]> stats = shipmentsProductsRepository.findDailyStatsByProductAndWarehouseBetweenDates(id, warehouse.getId(), start_date, end_date);
            Map<LocalDate, Long> demandMap = stats.stream().collect(Collectors.toMap(
                    row -> (LocalDate) row[0],
                    row -> (Long) row[1]
            ));

            List<String> dates = new ArrayList<>();
            List<Integer> values = new ArrayList<>();
            for (LocalDate date = start_date; !date.isAfter(end_date); date = date.plusDays(1)) {
                dates.add(date.toString());
                values.add(demandMap.getOrDefault(date, 0L).intValue());
            }

            dateLabelsByWarehouse.put(warehouse.getId(), dates);
            demandDataByWarehouse.put(warehouse.getId(), values);
        }

        List<Object[]> allStats = shipmentsProductsRepository.findDailyStatsByProductBetweenDates(id, start_date, end_date);
        Map<LocalDate, Long> allDemandMap = allStats.stream().collect(Collectors.toMap(
                row -> (LocalDate) row[0],
                row -> (Long) row[1]
        ));
        List<String> allDates = new ArrayList<>();
        List<Integer> allValues = new ArrayList<>();
        for (LocalDate date = start_date; !date.isAfter(end_date); date = date.plusDays(1)) {
            allDates.add(date.toString());
            allValues.add(allDemandMap.getOrDefault(date, 0L).intValue());
        }
        dateLabelsByWarehouse.put(0L, allDates);
        demandDataByWarehouse.put(0L, allValues);

        model.addAttribute("warehouses", allWarehouses);
        model.addAttribute("demandDataByWarehouse", demandDataByWarehouse);
        model.addAttribute("dateLabelsByWarehouse", dateLabelsByWarehouse);

        model.addAttribute("stockLabels", stockLabels);
        model.addAttribute("stockValues", stockValues);
        model.addAttribute("demandLabels", demandLabels);
        model.addAttribute("demandValues", demandValues);
        model.addAttribute("productId", id);

        return "product";
    }

}
