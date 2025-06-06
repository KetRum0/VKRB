package com.maidiploma.supplychainapp.repository;

import com.maidiploma.supplychainapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
     Optional<Product> findBySku(String sku);

     boolean existsBySku(String sku);
}
