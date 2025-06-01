package com.maidiploma.supplychainapp.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoricalData {

    private List<LocalDate> dates = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();

    public void addEntry(LocalDate date, Integer quantity) {
        dates.add(date);
        quantities.add(quantity);
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
    }
}
