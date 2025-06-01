package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SupplierRepository extends JpaRepository<Supplier, Long> {}
