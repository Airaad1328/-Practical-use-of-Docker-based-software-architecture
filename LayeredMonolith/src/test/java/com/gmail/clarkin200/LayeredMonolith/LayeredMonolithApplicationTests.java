package com.gmail.clarkin200.LayeredMonolith;

import com.gmail.clarkin200.LayeredMonolith.application.ProductServiceImpl;
import com.gmail.clarkin200.LayeredMonolith.domain.Product;
import com.gmail.clarkin200.LayeredMonolith.infrastracture.ProductSQLRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LayeredMonolithApplicationTests {

    @Mock
    ProductSQLRepository sqlRepository;

    @Mock
    RedisTemplate<String, Product> redisTemplate;

    @Mock
    ValueOperations<String, Product> valueOps;

    @InjectMocks
    ProductServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Method init = ProductServiceImpl.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(service);
        clearInvocations(redisTemplate, valueOps);
    }

    @Test
    void save_whenSqlSaves_shouldPutToRedisWithTtlAndReturnProduct() {
        Product p = new Product();
        p.setId(10);

        when(sqlRepository.save(p)).thenReturn(p);

        Optional<Product> res = service.save(p);

        assertTrue(res.isPresent());
        assertSame(p, res.get());

        verify(sqlRepository).save(p);
        verify(valueOps).set(eq("products:10"), same(p), eq(Duration.ofSeconds(30)));
        verifyNoMoreInteractions(sqlRepository, valueOps);
    }

    @Test
    void save_whenSqlReturnsNull_shouldNotTouchRedis_andReturnEmpty() {
        Product p = new Product();
        p.setId(10);

        when(sqlRepository.save(p)).thenReturn(null);

        Optional<Product> res = service.save(p);

        assertTrue(res.isEmpty());
        verify(sqlRepository).save(p);
        verifyNoInteractions(valueOps);
    }

    @Test
    void getById_whenExistsInRedis_shouldReturnFromRedis_andNotHitSql() {
        when(redisTemplate.hasKey("products:5")).thenReturn(true);

        Product cached = new Product();
        cached.setId(5);
        when(valueOps.get("products:5")).thenReturn(cached);

        Optional<Product> res = service.getById(5);

        assertTrue(res.isPresent());
        assertSame(cached, res.get());

        verify(redisTemplate).hasKey("products:5");
        verify(valueOps).get("products:5");

        verifyNoInteractions(sqlRepository);
        verify(valueOps, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void getById_whenNotInRedis_butExistsInSql_shouldCacheWithTtl_andReturn() {
        when(redisTemplate.hasKey("products:7")).thenReturn(false);
        when(sqlRepository.existsById(7)).thenReturn(true);

        Product dbProduct = new Product();
        dbProduct.setId(7);
        when(sqlRepository.findById(7)).thenReturn(Optional.of(dbProduct));

        Optional<Product> res = service.getById(7);

        assertTrue(res.isPresent());
        assertSame(dbProduct, res.get());

        verify(redisTemplate).hasKey("products:7");
        verify(sqlRepository).existsById(7);
        verify(sqlRepository).findById(7);

        verify(valueOps).set(eq("products:7"), same(dbProduct), eq(Duration.ofSeconds(30)));
    }

    @Test
    void getById_whenNotInRedis_andNotInSql_shouldReturnEmpty_andNotCache() {
        when(redisTemplate.hasKey("products:9")).thenReturn(false);
        when(sqlRepository.existsById(9)).thenReturn(false);

        Optional<Product> res = service.getById(9);

        assertTrue(res.isEmpty());

        verify(redisTemplate).hasKey("products:9");
        verify(sqlRepository).existsById(9);
        verify(sqlRepository, never()).findById(anyInt());
        verify(valueOps, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void fetchAll_shouldReturnFromSqlOnly() {
        Product p1 = new Product(); p1.setId(1);
        Product p2 = new Product(); p2.setId(2);

        when(sqlRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Product> res = service.fetchAll();

        assertEquals(2, res.size());
        verify(sqlRepository).findAll();
        verifyNoInteractions(redisTemplate, valueOps);
    }

}
