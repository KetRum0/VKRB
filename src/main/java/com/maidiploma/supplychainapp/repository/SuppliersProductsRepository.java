package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Supplier;
import com.maidiploma.supplychainapp.model.compositeKeys.SupplierProductId;
import com.maidiploma.supplychainapp.model.SuppliersProducts;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository

public interface SuppliersProductsRepository extends JpaRepository<SuppliersProducts, SupplierProductId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SuppliersProducts dp WHERE dp.id.product = :product")
    void deleteAllByProduct(@Param("product") Product product);

    List<SuppliersProducts> findById_SupplierId(Long id);

    @Query("SELECT sp.id.supplier FROM SuppliersProducts sp WHERE sp.id.product.id = :productId")
    List<Supplier> findSuppliersByProductId(@Param("productId") Long productId);

    @Query("SELECT sp.price FROM SuppliersProducts sp WHERE sp.id.supplier.id = :supplierId AND sp.id.product.id = :productId")
    Optional<BigDecimal> findPriceBySupplierAndProduct(@Param("supplierId") Long supplierId,
                                                       @Param("productId") Long productId);

}
