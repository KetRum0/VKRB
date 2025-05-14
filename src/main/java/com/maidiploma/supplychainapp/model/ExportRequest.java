package com.maidiploma.supplychainapp.model;

import java.util.List;

public class ExportRequest {
    private Long warehouseId;
    private List<OrderData> orders;

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<OrderData> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderData> orders) {
        this.orders = orders;
    }
}