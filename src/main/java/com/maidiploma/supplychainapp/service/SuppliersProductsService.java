package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Supplier;
import com.maidiploma.supplychainapp.model.SuppliersProducts;
import com.maidiploma.supplychainapp.model.compositeKeys.SupplierProductId;
import com.maidiploma.supplychainapp.repository.SuppliersProductsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SuppliersProductsService {
    private final SuppliersProductsRepository suppliersProductsRepository;

    public SuppliersProductsService(SuppliersProductsRepository suppliersProductsRepository) {
        this.suppliersProductsRepository = suppliersProductsRepository;
    }

    public List<SuppliersProducts> findById_SupplierId(Long id) {
        return suppliersProductsRepository.findById_SupplierId(id);
    }

    public void save(SuppliersProducts sp) {
        suppliersProductsRepository.save(sp);
    }

    public boolean existsById(SupplierProductId idComposite) {
        return suppliersProductsRepository.existsById(idComposite);
    }

    public SuppliersProducts findById(SupplierProductId key) {
        return suppliersProductsRepository.findById(key).orElseThrow();
    }

    public void deleteById(SupplierProductId idP) {
        suppliersProductsRepository.deleteById(idP);
    }

    public List<Supplier> findSuppliersByProductId(Long productId) {
        return suppliersProductsRepository.findSuppliersByProductId(productId);
    }

    public BigDecimal findPriceBySupplierAndProduct(Long supplier, Long product) {
        return suppliersProductsRepository.findPriceBySupplierAndProduct(supplier, product).orElseThrow();
    }
}
