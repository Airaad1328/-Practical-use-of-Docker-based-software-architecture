package com.gmail.clarkin200.LayeredMonolith.application;

import com.gmail.clarkin200.LayeredMonolith.domain.Product;
import com.gmail.clarkin200.LayeredMonolith.infrastracture.ProductSQLRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements BaseService<Integer, Product> {

    private final Duration TTL =  Duration.ofSeconds(30);
    private final String KEY = "products:";

    private ProductSQLRepository SQLRepository;
    private RedisTemplate<String,Product> redisTemplate;
    private ValueOperations<String,Product> redisOperation;


    public ProductServiceImpl (ProductSQLRepository SQLRepository,
                               RedisTemplate<String,Product> redisTemplate) {
        this.SQLRepository = SQLRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init () {
        redisOperation = redisTemplate.opsForValue();
    }

    @Override
    public Optional<Product> save(Product entity) {
        Optional<Product> toSaveOpt = Optional.ofNullable(SQLRepository.save(entity));
        if (toSaveOpt.isPresent()) {
            Product toSave = toSaveOpt.get();
            redisOperation.set(KEY+toSave.getId(),toSave,TTL);
        }
        return toSaveOpt;
    }

    @Override
    public Optional<Product> getById(Integer id) {
        if(redisTemplate.hasKey(KEY + id)) {
            return Optional.of(redisOperation.get(KEY+id));
        }
        if (SQLRepository.existsById(id)){
            Optional<Product> product = SQLRepository.findById(id);
            redisOperation.set(KEY + product.get().getId(),product.get(),TTL);
            return product;
        }
        return Optional.empty();
    }


    @Override
    public List<Product> fetchAll() {
        return SQLRepository.findAll();
    }
}
