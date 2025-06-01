package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.SupplyChainNode;
import com.maidiploma.supplychainapp.repository.SupplyChainNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplyChainNodeService {
    private final SupplyChainNodeRepository supplyChainNodeRepository;

    public SupplyChainNodeService(SupplyChainNodeRepository supplyChainNodeRepository) {
        this.supplyChainNodeRepository = supplyChainNodeRepository;
    }

    public List<SupplyChainNode> getAll() {
        return supplyChainNodeRepository.findAll();
    }

    public SupplyChainNode findById(Long id) {
        return supplyChainNodeRepository.findById(id).orElseThrow(() -> new RuntimeException("Не найден узел с id: " + id));

    }

    public void save(SupplyChainNode supplyChainNode) {
        supplyChainNodeRepository.save(supplyChainNode);
    }
}
