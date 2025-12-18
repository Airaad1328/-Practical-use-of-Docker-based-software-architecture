package com.gmail.clarkin200.LayeredMonolith.infrastracture;

import com.gmail.clarkin200.LayeredMonolith.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSQLRepository extends JpaRepository<Product, Integer> {
}
