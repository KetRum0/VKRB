package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.Delivery;
import com.maidiploma.supplychainapp.model.DeliveriesProducts;
import com.maidiploma.supplychainapp.repository.DeliveriesProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveriesProductsService {

    private final DeliveriesProductsRepository deliveriesProductsRepository;

    public List<DeliveriesProducts> findByDelivery(Delivery delivery) {
        return deliveriesProductsRepository.findById_Delivery(delivery);
    }

    public List<DeliveriesProducts> findById_Delivery_Id(Long id) {
        return deliveriesProductsRepository.findById_Delivery_Id(id);
    }
}