package com.maidiploma.supplychainapp.model.compositeKeys;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Supplier;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupplierProductId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierProductId that = (SupplierProductId) o;
        return Objects.equals(supplier, that.supplier) &&
                Objects.equals(product, that.product); // и так далее
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplier, product); // все поля
    }

}
