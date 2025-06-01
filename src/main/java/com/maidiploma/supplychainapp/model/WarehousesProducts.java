package com.maidiploma.supplychainapp.model;

import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "warehouses_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehousesProducts {
    @EmbeddedId
    private WarehouseProductId id;

    @Column(nullable = false)
    private Integer quantity;

}
