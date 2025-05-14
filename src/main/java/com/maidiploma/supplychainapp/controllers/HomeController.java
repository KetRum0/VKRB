package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    private final WarehouseService warehouseService;
    private final DeliveryService deliveryService;
    private final ShipmentService shipmentService;
    private final SupplyChainNodeService supplyChainNodeService;
    private final WarehousesProductsService warehousesProductsService;
    private final CalculateService calculateService;

    public HomeController(
            WarehouseService warehouseService,
            DeliveryService deliveryService,
            ShipmentService shipmentService,
            SupplyChainNodeService supplyChainNodeService,
            WarehousesProductsService warehousesProductsService,
            CalculateService calculateService) {
        this.warehouseService = warehouseService;
        this.deliveryService = deliveryService;
        this.shipmentService = shipmentService;
        this.supplyChainNodeService = supplyChainNodeService;
        this.warehousesProductsService = warehousesProductsService;
        this.calculateService = calculateService;
    }

    @GetMapping("/home")
    public String getHomePage(Model model) {

        List<Object[]> result = warehousesProductsService.getTotalOccupiedPerDay();
        List<SupplyChainNode> nodes = supplyChainNodeService.getAll();
        List<Warehouse> warehouses = warehouseService.getAll();
        List<Delivery> deliveries = deliveryService.getAll();
        List<Shipment> shipments = shipmentService.findAll();

        model.addAttribute("shipments", shipments);
        model.addAttribute("supplyData", result);
        model.addAttribute("nodes", nodes);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("deliveries", deliveries);

        return "home";
    }

    @GetMapping("/test")
    public String getHomePage() {

        calculateService.getOptimalOrder(1L);

        return "test";
    }
}




