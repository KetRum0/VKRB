package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.SupplyChainEdge;
import com.maidiploma.supplychainapp.model.SupplyChainNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyChainEdgeRepository extends JpaRepository<SupplyChainEdge, Long> {
    Optional<SupplyChainEdge> findByFromNodeIdAndToNodeId(SupplyChainNode fromNodeId, SupplyChainNode toNodeId);

    @Query("""
    SELECT w.id 
    FROM SupplyChainEdge e 
    JOIN e.toNodeId n 
    JOIN n.warehouse w 
    WHERE e.fromNodeId.supplier.id = :supplierId
    """)
    List<Long> findChildWarehouseIdsBySupplierId(@Param("supplierId") Long supplierId);

    @Query("""
    SELECT w.id
    FROM SupplyChainEdge e
    JOIN e.toNodeId n
    JOIN n.warehouse w
    WHERE e.fromNodeId.warehouse.id = :warehouseId
    """)
    List<Long> findChildWarehouseIdsByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("""
    SELECT e.length
    FROM SupplyChainEdge e
    WHERE e.fromNodeId.supplier.id = :supplierId
      AND e.toNodeId.warehouse.id = :warehouseId
    """)
    Optional<Integer> findEdgeLengthBySupplierIdAndWarehouseId(@Param("supplierId") Long supplierId,
                                                               @Param("warehouseId") Long warehouseId);

    @Query("""
    SELECT e.length
    FROM SupplyChainEdge e
    WHERE e.fromNodeId.warehouse.id = :warehouseId1
      AND e.toNodeId.warehouse.id = :warehouseId2
    """)
    Optional<Integer> findEdgeLengthByWarehouseIdAndWarehouseId(@Param("warehouseId1") Long warehouseId1,
                                                               @Param("warehouseId2") Long warehouseId2);

    Optional<SupplyChainEdge> findByToNodeIdWarehouseId(Long warehouseId);

    Optional<SupplyChainEdge> findByToNodeIdSupplierId(Long warehouseId);
}
