package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.ExportRequest;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.service.CalculateService;
import com.maidiploma.supplychainapp.model.ProductOpt;
import com.maidiploma.supplychainapp.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CalculateController {

    private final CalculateService calculateService;
    private final WarehouseService warehouseService;

    public CalculateController(CalculateService calculateService, WarehouseService warehouseService) {
        this.calculateService = calculateService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/planner")
    public String getCalculatePage(Model model)  {
        List<Warehouse> warehouses = warehouseService.getAll();
        List<ProductOpt> products = new ArrayList<>();

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("productsByWarehouse", products);

        return "calculate";
    }

    @PostMapping("/calculate/result")
    public String getResultPage(@RequestParam("s") int s,
                                @RequestParam("R") int R,
                                Model model)  {

        List<Warehouse> warehouses = warehouseService.getAll();
        Map<Long, List<ProductOpt>> productsByWarehouse = calculateService.calculateOrder(s, R);

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("productsByWarehouse", productsByWarehouse);

        return "calculate";
    }

    @PostMapping("result/export")
    public ResponseEntity<byte[]> exportToExcel(@RequestBody ExportRequest request) throws Exception {
       return calculateService.resultToExcel(request);
    }

}
