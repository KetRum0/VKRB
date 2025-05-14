package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Delivery;
import com.maidiploma.supplychainapp.model.DeliveriesProducts;
import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.compositeKeys.DeliveryProductId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository


public interface DeliveriesProductsRepository extends JpaRepository<DeliveriesProducts, DeliveryProductId> {
    List<DeliveriesProducts> findById_Delivery(Delivery delivery);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeliveriesProducts dp WHERE dp.id.delivery = :delivery")
    void deleteAllByDelivery(@Param("delivery") Delivery delivery);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeliveriesProducts dp WHERE dp.id.product = :product")
    void deleteAllByProduct(@Param("product") Product product);

    List<DeliveriesProducts> findById_Delivery_Id(Long id);
}
