package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Shipment;
import com.maidiploma.supplychainapp.model.ShipmentsProducts;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.model.compositeKeys.ShipmentProductId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository

public interface ShipmentsProductsRepository extends JpaRepository<ShipmentsProducts, ShipmentProductId> {
    List<ShipmentsProducts> findById_Shipment(Shipment shipment);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShipmentsProducts dp WHERE dp.id.shipment = :shipment")
    void deleteAllByShipment(@Param("shipment") Shipment shipment);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShipmentsProducts dp WHERE dp.id.product = :product")
    void deleteAllByProduct(@Param("product") Product product);

    List<ShipmentsProducts> findById_Shipment_Id(Long id);

    @Query("""
    SELECT w.name, SUM(sp.quantity)
    FROM ShipmentsProducts sp
    JOIN sp.id.shipment s
    JOIN s.warehouse w
    WHERE sp.id.product.id = :productId
    GROUP BY w.name
""")
    List<Object[]> findTotalProductShippedByWarehouseName(@Param("productId") Long productId);

    @Query("""
    SELECT s.shipmentDate, SUM(sp.quantity)
    FROM ShipmentsProducts sp
    JOIN sp.id.shipment s
    WHERE sp.id.product.id = :productId AND s.warehouse.id = :warehouseId
    GROUP BY s.shipmentDate
    ORDER BY s.shipmentDate
""")
    List<Object[]> findDailyShipmentByProductAndWarehouse(@Param("productId") Long productId,
                                                          @Param("warehouseId") Long warehouseId);




    @Query("""
    SELECT s.shipmentDate, SUM(sp.quantity)
    FROM ShipmentsProducts sp
    JOIN sp.id.shipment s
    WHERE sp.id.product.id = :productId
    GROUP BY s.shipmentDate
    ORDER BY s.shipmentDate
""")
    List<Object[]> findDailyStatsByProduct(@Param("productId") Long productId);


    @Query("SELECT s.shipmentDate, SUM(sp.quantity) " +
            "FROM ShipmentsProducts sp " +
            "JOIN sp.id.shipment s " +
            "WHERE sp.id.product.id = :productId AND s.warehouse.id = :warehouseId " +
            "GROUP BY s.shipmentDate " +
            "ORDER BY s.shipmentDate")
    List<Object[]> findDailyStatsByProductAndWarehouse(@Param("productId") Long productId,
                                                       @Param("warehouseId") Long warehouseId);


    @Query("SELECT s.shipmentDate, SUM(sp.quantity) " +
            "FROM ShipmentsProducts sp " +
            "JOIN sp.id.shipment s " +
            "WHERE sp.id.product.id = :productId " +
            "AND s.warehouse.id = :warehouseId " +
            "AND s.shipmentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.shipmentDate " +
            "ORDER BY s.shipmentDate")
    List<Object[]> findDailyStatsByProductAndWarehouseBetweenDates(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query("""
    SELECT s.shipmentDate, SUM(sp.quantity)
    FROM ShipmentsProducts sp
    JOIN sp.id.shipment s
    WHERE sp.id.product.id = :productId
    AND s.shipmentDate BETWEEN :startDate AND :endDate
    GROUP BY s.shipmentDate
    ORDER BY s.shipmentDate
""")
    List<Object[]> findDailyStatsByProductBetweenDates(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<ShipmentsProducts> findById_Shipment_Warehouse_IdAndId_Product_Id(Long warehouseId, Long productId);

    @Query("SELECT SUM(sp.quantity) FROM ShipmentsProducts sp " +
            "WHERE sp.id.shipment.warehouse.id = :warehouseId AND sp.id.product.id = :productId")
    Long findTotalQuantityForProductFromWarehouse(@Param("warehouseId") Long warehouseId,
                                                  @Param("productId") Long productId);


    @Query("SELECT SUM(sp.quantity) FROM ShipmentsProducts sp " +
            "WHERE sp.id.shipment.shipmentDate = :date " +
            "AND sp.id.shipment.warehouse = :warehouse " +
            "AND sp.id.product = :product")
    Integer sumQuantityByDateAndWarehouseAndProduct(@Param("date") LocalDate date,
                                                    @Param("warehouse") Warehouse warehouse,
                                                    @Param("product") Product product);

}
