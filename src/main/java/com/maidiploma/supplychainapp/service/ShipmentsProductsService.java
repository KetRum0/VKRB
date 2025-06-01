package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.model.compositeKeys.ShipmentProductId;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.ShipmentsProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentsProductsService {

    private final ShipmentsProductsRepository shipmentsProductsRepository;

    public List<ShipmentsProducts> findByShipment(Shipment shipment) {
        return shipmentsProductsRepository.findById_Shipment(shipment);
    }

    public List<ShipmentsProducts> findById_Shipment_Id(Long id) {
        return shipmentsProductsRepository.findById_Shipment_Id(id);
    }

    public Long getTotalQuantitySentFromWarehouse(Long warehouseId, Long productId) {
        Long total = shipmentsProductsRepository.findTotalQuantityForProductFromWarehouse(warehouseId, productId);
        return total != null ? total : 0L;
    }

    public BigDecimal getDev(Long endNodeId) {
        return BigDecimal.valueOf(100);
    }

    public HistoricalData getDemand(LocalDate startDate, LocalDate curDate, Warehouse warehouse, Product product) {
        HistoricalData hd = new HistoricalData();
        List<Integer> quantities = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(curDate); date = date.plusDays(1)) {
           dates.add(date);
            Integer quantity = shipmentsProductsRepository.sumQuantityByDateAndWarehouseAndProduct(date, warehouse, product);
            quantities.add(quantity != null ? quantity : 0);

        }
        hd.setDates(dates);
        hd.setQuantities(quantities);
        return hd;
    }
}
