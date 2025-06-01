package com.maidiploma.supplychainapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @ManyToOne
    @JoinColumn(name = "edge_id", nullable = false)
    private SupplyChainEdge edge_id;
}
