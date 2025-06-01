package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.model.WarehousesProducts;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface WarehousesProductsRepository extends JpaRepository<WarehousesProducts, WarehouseProductId> {

    @Query("SELECT wp.id.stockDate, SUM(wp.quantity) FROM WarehousesProducts wp GROUP BY wp.id.stockDate ORDER BY wp.id.stockDate")
    List<Object[]> getTotalOccupiedPerDay();

    @Query("""
    SELECT wp.id.stockDate, SUM(wp.quantity)
    FROM WarehousesProducts wp
    WHERE wp.id.warehouse.id = :warehouseId
    GROUP BY wp.id.stockDate
    ORDER BY wp.id.stockDate
""")
    List<Object[]> getTotalOccupiedPerDayByWarehouse(@Param("warehouseId") Long warehouseId);


    @Modifying
    @Transactional
    @Query("DELETE FROM WarehousesProducts dp WHERE dp.id.product = :product")
    void deleteAllByProduct(@Param("product") Product product);

    List<WarehousesProducts> findById_WarehouseId(Long id);

    List<WarehousesProducts> findById_Warehouse_IdAndId_StockDate(Long warehouseId, LocalDate stockDate);

    List<WarehousesProducts> findById_Warehouse_IdAndId_Product_IdOrderById_StockDate(Long warehouseId, Long productId);


    List<WarehousesProducts> findById_ProductAndId_StockDate(Product product, LocalDate date);

    @Query("""
    SELECT wp.id.warehouse.name, wp.quantity
    FROM WarehousesProducts wp
    WHERE wp.id.product.id = :productId AND wp.id.stockDate = :date
""")
    List<Object[]> findByProductIdAndDate(@Param("productId") Long productId,
                                          @Param("date") LocalDate date);

    @Query("SELECT wp.quantity FROM WarehousesProducts wp " +
            "WHERE wp.id.warehouse.id = :warehouseId AND wp.id.product.id = :productId AND wp.id.stockDate = :stockDate")
    Integer findQuantityByWarehouseIdAndProductIdAndStockDate(@Param("warehouseId") Long warehouseId,
                                                              @Param("productId") Long productId,
                                                              @Param("stockDate") LocalDate stockDate);

}
