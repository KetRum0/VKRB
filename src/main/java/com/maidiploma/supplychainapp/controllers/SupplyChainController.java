package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.Supplier;
import com.maidiploma.supplychainapp.model.SupplyChainEdge;
import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.repository.SupplierRepository;
import com.maidiploma.supplychainapp.repository.SupplyChainEdgeRepository;
import com.maidiploma.supplychainapp.repository.SupplyChainNodeRepository;
import com.maidiploma.supplychainapp.repository.WarehouseRepository;
import com.maidiploma.supplychainapp.service.ProductService;
import com.maidiploma.supplychainapp.service.SupplierService;
import com.maidiploma.supplychainapp.service.SuppliersProductsService;
import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class SupplyChainController {

    private final SupplyChainNodeRepository supplyChainNodeRepository;
    private final SupplyChainEdgeRepository supplyChainEdgeRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;

    public SupplyChainController(SupplyChainNodeRepository supplyChainNodeRepository, SupplyChainEdgeRepository supplyChainEdgeRepository, WarehouseRepository warehouseRepository, SupplierRepository supplierRepository) {
        this.supplyChainNodeRepository = supplyChainNodeRepository;
        this.supplyChainEdgeRepository = supplyChainEdgeRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
    }

    @GetMapping("/supplychain")
    public String getSupplyChain(Model model) {

        List<SupplyChainNode> nodes = supplyChainNodeRepository.findAll();
        List<SupplyChainEdge> edges = supplyChainEdgeRepository.findAll();
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();

        model.addAttribute("nodes", nodes);
        model.addAttribute("edges", edges);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("suppliers", suppliers);

            return "supplychain";
    }

    @PostMapping("/edges/edit")
    public String editEdge(@RequestParam Long id,
                           @RequestParam int length) {

        Optional<SupplyChainEdge> edgeOpt = supplyChainEdgeRepository.findById(id);
        if (edgeOpt.isPresent()) {
            SupplyChainEdge edge = edgeOpt.get();
            edge.setLength(length);
            supplyChainEdgeRepository.save(edge);
        }

        return "redirect:/supplychain";
    }

    @PostMapping("/edges/delete")
    public String deleteEdge(@RequestParam Long id) {

        supplyChainEdgeRepository.deleteById(id);

        return "redirect:/supplychain";
    }

    @PostMapping("/edges/add")
    public String addEdge(@RequestParam Long fromId,
                          @RequestParam Long toId,
                          @RequestParam int length) {

        SupplyChainNode fromNode = supplyChainNodeRepository.findById(fromId).orElseThrow(() -> new IllegalArgumentException("Invalid fromId: " + fromId));
        SupplyChainNode toNode = supplyChainNodeRepository.findById(toId).orElseThrow(() -> new IllegalArgumentException("Invalid toId: " + toId));
        SupplyChainEdge edge = new SupplyChainEdge(null, fromNode, toNode, length);
        supplyChainEdgeRepository.save(edge);

        return "redirect:/supplychain";
    }

}