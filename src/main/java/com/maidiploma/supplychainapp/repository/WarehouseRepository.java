package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {}
