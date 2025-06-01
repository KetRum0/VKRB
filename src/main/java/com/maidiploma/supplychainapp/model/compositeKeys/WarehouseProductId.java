package com.maidiploma.supplychainapp.model.compositeKeys;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class WarehouseProductId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "stock_date")
    private LocalDate stockDate;

    public LocalDate getStockDate() {
        return stockDate;
    }

    public void setStockDate(LocalDate stockDate) {
        this.stockDate = stockDate;
    }


    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WarehouseProductId that = (WarehouseProductId) obj;
        return Objects.equals(warehouse, that.warehouse) &&
                Objects.equals(product, that.product) &&
                Objects.equals(stockDate, that.stockDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouse, product, stockDate);
    }
}