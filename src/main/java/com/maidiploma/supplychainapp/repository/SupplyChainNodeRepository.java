package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.SupplyChainNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SupplyChainNodeRepository extends JpaRepository<SupplyChainNode, Long> {}
