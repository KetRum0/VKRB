package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Supplier;
import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.repository.SupplierRepository;
import com.maidiploma.supplychainapp.repository.SuppliersProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SupplierService {
    private final SupplyChainNodeService supplyChainNodeService;
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplyChainNodeService supplyChainNodeService, SupplierRepository supplierRepository, SuppliersProductsRepository suppliersProductsRepository) {
        this.supplyChainNodeService = supplyChainNodeService;
        this.supplierRepository = supplierRepository;
    }

    public void add(String sname, BigDecimal slattitude, BigDecimal slongitude) {
        Supplier supplier = new Supplier(null, sname, slattitude, slongitude);
        supplierRepository.save(supplier);

        SupplyChainNode supplyChainNode = new SupplyChainNode();
        supplyChainNode.setNodeType("supplier");
        supplyChainNode.setSupplier(supplier);

        supplyChainNodeService.save(supplyChainNode);
    }

    public void save(Long id, String name, BigDecimal slattitude, BigDecimal slongitude) {
        Supplier supplier = new Supplier(id, name, slattitude, slongitude);
        supplierRepository.save(supplier);

    }

    public void deleteById(Long id) {
        supplierRepository.deleteById(id);
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id).orElseThrow();
    }



}
