package com.maidiploma.supplychainapp.model;

import com.maidiploma.supplychainapp.model.compositeKeys.DeliveryProductId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "delivery_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveriesProducts {
    @EmbeddedId
    private DeliveryProductId id;

    @Column(nullable = false)
    private Integer quantity;
}
