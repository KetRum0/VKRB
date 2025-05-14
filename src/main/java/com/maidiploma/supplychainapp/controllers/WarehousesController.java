package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.model.WarehousesProducts;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.SupplyChainNodeRepository;
import com.maidiploma.supplychainapp.repository.WarehouseRepository;
import com.maidiploma.supplychainapp.repository.WarehousesProductsRepository;
import com.maidiploma.supplychainapp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/warehouses")
public class WarehousesController {

    @Autowired
    private final WarehouseService warehouseService;
    @Autowired
    private WarehousesProductsService warehousesProductsService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private ProductService productService;

    public WarehousesController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/add")
    public String addWarehouse(@RequestParam String Wname,
                               @RequestParam Integer Wcapacity,
                               @RequestParam BigDecimal Wlongitude,
                               @RequestParam BigDecimal Wlattitude) {

        warehouseService.add(Wname, Wcapacity, Wlattitude, Wlongitude);

        return "redirect:/supplychain";
    }

    @PostMapping("/edit")
    public String editWarehouse(@RequestParam Long id,
                                @RequestParam String Wname,
                                @RequestParam Integer Wcapacity,
                                @RequestParam BigDecimal Wlongitude,
                                @RequestParam BigDecimal Wlattitude) {

        warehouseService.edit(id, Wname, Wcapacity, Wlattitude, Wlongitude);
        return "redirect:/supplychain";
    }

    @PostMapping("/delete/{id}")
    public String deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteById(id);
        return "redirect:/supplychain";
    }

    @PostMapping("/history/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, @RequestParam("warehouseId") Long warehouseId) throws IOException {
        warehousesProductsService.importExcel(file, warehouseId);
        return "redirect:/warehouses/view/" + warehouseId;

    }

    @PostMapping("/products/save")
    public String importFromExcel( @RequestParam("warehouseId") Long warehouseId, @RequestParam("quantity") Integer quantity, @RequestParam("productId") Long productId) throws IOException {
        WarehousesProducts warehousesProducts = new WarehousesProducts();
        WarehouseProductId warehouseProductId = new WarehouseProductId();
        warehouseProductId.setProduct(productService.getById(productId));
        warehouseProductId.setWarehouse(warehouseService.findById(warehouseId));
        warehouseProductId.setStockDate(settingsService.getEnd());
        warehousesProducts.setId(warehouseProductId);
        warehousesProducts.setQuantity(quantity);
        warehousesProductsService.save(warehousesProducts);
        return "redirect:/warehouses/view/" + warehouseId;

    }


    @GetMapping("/view/{id}")
    public String viewWarehouse(@PathVariable Long id, Model model) {

        List<WarehousesProducts> products = warehousesProductsService.findById_Warehouse_IdAndId_StockDate(id, settingsService.getEnd());
        Warehouse warehouse = warehouseService.findById(id);
        List<Object[]> results = warehousesProductsService.getTotalOccupiedPerDayByWarehouse(id);
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long totalQuantity = (Long) row[1];
            labels.add(date.format(formatter));
            values.add(totalQuantity.intValue());
        }

        List<WarehousesProducts> wp= warehousesProductsService.getAll();
        model.addAttribute("warehouse", warehouse);
        model.addAttribute("products", products);
        model.addAttribute("wp", wp);
        model.addAttribute("curDate", settingsService.getEnd());
        model.addAttribute("labels", labels);
        model.addAttribute("values", values);
        return "warehouse";
    }
    @GetMapping("/{warehouseId}/product/{productId}/history")
    @ResponseBody
    public List<Map<String, Object>> getProductStockHistory(
            @PathVariable Long warehouseId,
            @PathVariable Long productId) {

        List<WarehousesProducts> history = warehousesProductsService
                .findById_Warehouse_IdAndId_Product_IdOrderById_StockDate(warehouseId, productId);

        return history.stream().map(wp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", wp.getId().getStockDate().format(DateTimeFormatter.ofPattern("dd.MM")));
            map.put("quantity", wp.getQuantity());
            return map;
        }).collect(Collectors.toList());
    }


}
