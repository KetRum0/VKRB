package com.maidiploma.supplychainapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "supplychain_nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyChainNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String nodeType;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "warehouse_id"))
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "supplier_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "supplier_id"))
    private Supplier supplier;


    public String getName() {
        if (warehouse != null) {
            return warehouse.getName();
        } else if (supplier != null) {
            return supplier.getName();
        }
        return "Неизвестно";
    }

    public BigDecimal getLatitude() {
        if (warehouse != null) {
            return warehouse.getLatitude();
        } else if (supplier != null) {
            return supplier.getLatitude();
        }
        return null;
    }
    public BigDecimal getLongitude() {
        if (warehouse != null) {
            return warehouse.getLongitude();
        } else if (supplier != null) {
            return supplier.getLongitude();
        }
        return null;
    }

}

