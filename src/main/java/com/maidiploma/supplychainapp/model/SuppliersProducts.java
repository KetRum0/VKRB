package com.maidiploma.supplychainapp.model;

import com.maidiploma.supplychainapp.model.compositeKeys.SupplierProductId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "suppliers_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuppliersProducts {

    @EmbeddedId
    private SupplierProductId id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
