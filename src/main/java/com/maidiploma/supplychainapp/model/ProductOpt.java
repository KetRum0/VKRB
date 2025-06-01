package com.maidiploma.supplychainapp.model;

import java.util.ArrayList;
import java.util.List;

public class ProductOpt {
    private Long id;
    private String code;
    private String name;
    private int forecast;
    private int safetyStock;
    private int optimalStock;
    private int optimalOrder;
    private String category;
    private double mean;
    private double dev;
    private int I;
    private List<Integer> d;

    public ProductOpt(Long id, String code, String name, int forecast, int safetyStock, int optimalStock, int optimalOrder) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.forecast = forecast;
        this.safetyStock = safetyStock;
        this.optimalStock = optimalStock;
        this.optimalOrder = optimalOrder;
        this.category = "";
        this.mean = 0;
        this.dev = 0;
        this.I = 0;
        d = new ArrayList<>();

    }

    public ProductOpt() {
        this.id = 0L;
        this.code = "0";
        this.name = "0";
        this.forecast = 0;
        this.safetyStock = 0;
        this.optimalStock = 0;
        this.optimalOrder = 0;
        this.category = "";
        this.mean = 0;
        this.dev = 0;
        this.I = 0;
        d= new ArrayList<>();

    }

    public List<Integer> getD() {
        return d;
    }

    public void setD(List<Integer> d) {
        this.d = d;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getDev() {
        return dev;
    }

    public void setDev(double dev) {
        this.dev = dev;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getForecast() {
        return forecast;
    }

    public void setForecast(int forecast) {
        this.forecast = forecast;
    }

    public int getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(int safetyStock) {
        this.safetyStock = safetyStock;
    }

    public int getOptimalStock() {
        return optimalStock;
    }

    public void setOptimalStock(int optimalStock) {
        this.optimalStock = optimalStock;
    }

    public int getOptimalOrder() {
        return optimalOrder;
    }

    public void setOptimalOrder(int optimalOrder) {
        this.optimalOrder = optimalOrder;
    }

    public int getI() {
        return I;
    }

    public void setI(int i) {
        I = i;
    }
}
