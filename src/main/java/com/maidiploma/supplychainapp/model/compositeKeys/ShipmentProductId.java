package com.maidiploma.supplychainapp.model.compositeKeys;
import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Shipment;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ShipmentProductId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipmentProductId that = (ShipmentProductId) o;
        return Objects.equals(shipment, that.shipment) &&
                Objects.equals(product, that.product); // и так далее
    }

    @Override
    public int hashCode() {
        return Objects.hash(shipment, product); // все поля
    }

}