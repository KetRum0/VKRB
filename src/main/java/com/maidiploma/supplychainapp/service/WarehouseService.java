package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final SupplyChainNodeService supplyChainNodeService;
    private final SettingsService settingsService;
    private final WarehousesProductsService warehousesProductsService;
    private final ProductService productService;

    public WarehouseService(WarehouseRepository warehouseRepository, SupplyChainNodeService supplyChainNodeService, SettingsService settingsService, WarehousesProductsService warehousesProductsService, ProductService productService) {
        this.warehouseRepository = warehouseRepository;
        this.supplyChainNodeService = supplyChainNodeService;
        this.settingsService = settingsService;
        this.warehousesProductsService = warehousesProductsService;
        this.productService = productService;
    }

    public List<Warehouse> getAll() {
        return warehouseRepository.findAll();
    }

    public void save(Warehouse warehouse) {
        warehouseRepository.save(warehouse);
    }

    public void add(String Wname, Integer Wcapacity, BigDecimal Wlattitude, BigDecimal Wlongitude, BigDecimal wholdingcost) {
        Warehouse warehouse = new Warehouse(null, Wname, Wcapacity, Wlattitude, Wlongitude, wholdingcost);
        warehouseRepository.save(warehouse);
        SupplyChainNode supplyChainNode = new SupplyChainNode();
        supplyChainNode.setNodeType("warehouse");
        supplyChainNode.setWarehouse(warehouse);
        supplyChainNodeService.save(supplyChainNode);
        warehousesProductsService.fillZeroStock(settingsService.getStart(), settingsService.getEnd(), List.of(warehouse), productService.getAll());

    }

    public void edit(Long id, String name, Integer capacity, BigDecimal Wlattitude, BigDecimal Wlongitude, BigDecimal wholdingcost) {
        Warehouse warehouse = new Warehouse(id, name, capacity, Wlattitude, Wlongitude, wholdingcost);
        warehouseRepository.save(warehouse);

    }

    public void deleteById(Long id) {
        warehouseRepository.deleteById(id);
    }

    public Warehouse findById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).orElseThrow();
    }
}
