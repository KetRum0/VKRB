package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.ShipmentsProducts;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.service.ShipmentService;
import com.maidiploma.supplychainapp.service.ShipmentsProductsService;
import com.maidiploma.supplychainapp.service.WarehouseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ShipmentsController {

    private final ShipmentsProductsService shipmentsProductsService;
    private final WarehouseService warehouseService;
    private final ShipmentService shipmentService;


    public ShipmentsController(ShipmentsProductsService shipmentsProductsService, WarehouseService warehouseService, ShipmentService shipmentService) {
        this.shipmentsProductsService = shipmentsProductsService;
        this.warehouseService = warehouseService;
        this.shipmentService = shipmentService;
    }


    @GetMapping("/shipments/download/{id}")
    public void downloadShipmentExcel(@PathVariable Long id, HttpServletResponse response) throws IOException {
        shipmentService.exportExcel(response, id);
    }



    @PostMapping("shipments/delete/{id}")
    public String deleteShipment(@PathVariable Long id) {

        shipmentService.deleteShipment(id);

        return "redirect:/home";
    }

    @GetMapping("shipments/view/{id}")
    public String viewShipment(@PathVariable Long id, Model model) {

        List<ShipmentsProducts> products = shipmentsProductsService.findById_Shipment_Id(id);
        model.addAttribute("shipments", products);

        return "shipment";
    }


    @PostMapping("shipments/add")
    public String uploadDelivery(
            @RequestParam("shipmentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Warehouse warehouse = warehouseService.findById(warehouseId);
        shipmentService.saveShipmentWithProducts(date, warehouse, file);

        return "redirect:/home";
    }

}
