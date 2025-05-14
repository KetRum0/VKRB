package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.SupplyChainEdge;
import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.repository.SupplyChainEdgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplyChainEdgeService {
    private final SupplyChainEdgeRepository supplyChainEdgeRepository;

    public SupplyChainEdgeService(SupplyChainEdgeRepository supplyChainEdgeRepository) {
        this.supplyChainEdgeRepository = supplyChainEdgeRepository;
    }

    public SupplyChainEdge findByFromNodeIdAndToNodeId(SupplyChainNode fromNode, SupplyChainNode toNode) {
        return supplyChainEdgeRepository.findByFromNodeIdAndToNodeId(fromNode, toNode).orElseThrow(() -> new RuntimeException("Не найдена связь от узла " + fromNode + " к " + toNode));
    }

    public List<SupplyChainEdge> getAll() {
        return supplyChainEdgeRepository.findAll();
    }

    public List<Long> findChildWarehouseIdsBySupplierId(Long supplierId) {
        return  supplyChainEdgeRepository.findChildWarehouseIdsBySupplierId(supplierId);
    }

    public List<Long> findChildWarehouseIdsByWarehouseId(Long warehouseId) {
        return  supplyChainEdgeRepository.findChildWarehouseIdsByWarehouseId(warehouseId);
    }

    Integer findEdgeLengthBySupplierIdAndWarehouseId(Long supplierId, Long warehouseId) {
        return supplyChainEdgeRepository.findEdgeLengthBySupplierIdAndWarehouseId(supplierId, warehouseId).orElseThrow();
    }

    Integer findEdgeLengthByWarehouseIdAndWarehouseId(Long warehouseId1, Long warehouseId2) {
        return supplyChainEdgeRepository.findEdgeLengthByWarehouseIdAndWarehouseId(warehouseId1, warehouseId2).orElseThrow();
    }

    public Optional<SupplyChainEdge> findEdgeByToWarehouseId(Long warehouseId) {
        return supplyChainEdgeRepository.findByToNodeIdWarehouseId(warehouseId);
    }

}
