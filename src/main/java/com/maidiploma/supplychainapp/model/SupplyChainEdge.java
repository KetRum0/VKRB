package com.maidiploma.supplychainapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "supplychain_edges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyChainEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_node_id")
    private SupplyChainNode fromNodeId;

    @ManyToOne
    @JoinColumn(name = "to_node_id")
    private SupplyChainNode toNodeId;

    @Column(nullable = false)
    private Integer length;
}
