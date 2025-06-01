package com.maidiploma.supplychainapp.model.compositeKeys;

import com.maidiploma.supplychainapp.model.Delivery;
import com.maidiploma.supplychainapp.model.Product;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeliveryProductId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;


    @ManyToOne
    @JoinColumn(name = "deliveries_id")
    private Delivery delivery;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryProductId that = (DeliveryProductId) o;
        return Objects.equals(product, that.product) &&
                Objects.equals(delivery, that.delivery); // и так далее
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, delivery); // все поля
    }
    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}