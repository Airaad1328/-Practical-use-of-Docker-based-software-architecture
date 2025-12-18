package com.gmail.clarkin200.LayeredMonolith.presentation;

import com.gmail.clarkin200.LayeredMonolith.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product dtoToEntity (CreateProductDto dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setPrice(dto.price());
        return product;
    }
}
