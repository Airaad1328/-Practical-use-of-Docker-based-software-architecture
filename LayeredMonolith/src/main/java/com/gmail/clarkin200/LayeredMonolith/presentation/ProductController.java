package com.gmail.clarkin200.LayeredMonolith.presentation;

import com.gmail.clarkin200.LayeredMonolith.application.BaseService;
import com.gmail.clarkin200.LayeredMonolith.application.ProductServiceImpl;
import com.gmail.clarkin200.LayeredMonolith.domain.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    private BaseService service;
    private ProductMapper mapper;

    public ProductController (ProductServiceImpl service,
                              ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById (@PathVariable Integer id) {
        Optional<Product> product = service.getById(id);
        return product.isPresent() ?
                ResponseEntity.ok(product.get())
                :
                ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> listProduct () {
        List<Product> products = service.fetchAll();
        return products.isEmpty() ?
                ResponseEntity.noContent().build()
                :
                ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct (@RequestBody CreateProductDto dto) {
        Optional<Product> created = service.save(mapper.dtoToEntity(dto));
        return created.isPresent() ?
                ResponseEntity.status(HttpStatusCode.valueOf(201)).body(created.get())
                :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
