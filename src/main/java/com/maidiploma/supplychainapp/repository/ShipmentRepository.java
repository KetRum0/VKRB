package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Shipment;
import com.maidiploma.supplychainapp.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByShipmentDateAndWarehouse(LocalDate shipmentDate, Warehouse warehouse);
}
