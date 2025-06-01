package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.DeliveriesProducts;
import com.maidiploma.supplychainapp.model.SupplyChainEdge;
import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.service.*;
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
public class DeliveriesController {

    private final DeliveriesProductsService deliveriesProductsService;
    private final DeliveryService deliveryService;
    private final SupplyChainEdgeService supplyChainEdgeService;
    private final SupplyChainNodeService supplyChainNodeService;

    public DeliveriesController(
            DeliveriesProductsService deliveriesProductsService,
            DeliveryService deliveryService,
            SupplyChainEdgeService supplyChainEdgeService,
            SupplyChainNodeService supplyChainNodeService
    ) {
        this.deliveriesProductsService = deliveriesProductsService;
        this.supplyChainEdgeService = supplyChainEdgeService;
        this.deliveryService = deliveryService;
        this.supplyChainNodeService = supplyChainNodeService;
    }

    @GetMapping("/deliveries/download/{id}")
    public void downloadDeliveryExcel(@PathVariable Long id, HttpServletResponse response) throws IOException {

        deliveryService.downloadExcel(id, response);

    }

    @PostMapping("deliveries/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {

        deliveryService.deleteDelivery(id);

        return "redirect:/home";
    }

    @GetMapping("deliveries/view/{id}")
    public String viewDelivery(@PathVariable Long id, Model model) {

        List<DeliveriesProducts> deliveries = deliveriesProductsService.findById_Delivery_Id(id);
        model.addAttribute("deliveries", deliveries);

        return "delivery";
    }

    @PostMapping("deliveries/add")
    public String uploadDelivery(
            @RequestParam("deliveryDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("fromNodeId") Long fromId,
            @RequestParam("toNodeId") Long toId,
            @RequestParam("expectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateExpected,
            @RequestParam("file") MultipartFile file) throws IOException {

        SupplyChainNode fromNode = supplyChainNodeService.findById(fromId);
        SupplyChainNode toNode = supplyChainNodeService.findById(toId);
        SupplyChainEdge edge = supplyChainEdgeService.findByFromNodeIdAndToNodeId(fromNode, toNode);
        deliveryService.saveDeliveryWithProducts(date, dateExpected, fromId, toId, file, edge);

        return "redirect:/home";
    }

}
