package com.maidiploma.supplychainapp.model;

import com.maidiploma.supplychainapp.model.compositeKeys.ShipmentProductId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "shipment_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsProducts {
    @EmbeddedId
    private ShipmentProductId id;

    @Column(nullable = false)
    private Integer quantity;


}
