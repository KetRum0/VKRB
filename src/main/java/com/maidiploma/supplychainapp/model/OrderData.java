package com.maidiploma.supplychainapp.model;

public class OrderData {
    private String productId;
    private String finalOrderSize;

    public String getFinalOrderSize() {
        return finalOrderSize;
    }

    public void setFinalOrderSize(String finalOrderSize) {
        this.finalOrderSize = finalOrderSize;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}