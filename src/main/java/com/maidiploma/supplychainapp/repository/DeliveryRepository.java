package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("""
        SELECT CAST(d.deliveryDate AS date) - CAST(d.expectedDate AS date)
        FROM Delivery d
        JOIN d.edge_id e
        JOIN e.fromNodeId fromNode
        JOIN e.toNodeId toNode
        WHERE 
            (fromNode.warehouse.id = :warehouseId AND toNode.warehouse.id = :toWarehouseId)
            OR
            (fromNode.supplier.id = :supplierId AND toNode.warehouse.id = :toWarehouseId)
    """)
    List<Duration> findDeliveryDelaysBetweenWarehouseAndSupplierOrWarehouse(
                @Param("warehouseId") Long warehouseId,
                @Param("supplierId") Long supplierId,
                @Param("toWarehouseId") Long toWarehouseId
        );
    }



