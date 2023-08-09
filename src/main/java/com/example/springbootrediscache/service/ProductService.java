package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * this service wrote for config cache using RedisTemplate class
 * and opsForValue method
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final static String cacheKey = "Product";
    private final static TimeUnit timeUnit = TimeUnit.MINUTES;
    private final static long timeout = 10;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public List<Product> getProducts() {
        List<Product> cachedData = getCacheData(cacheKey);
        if (cachedData != null) {
            return cachedData;
        } else {
            List<Product> dataFromDatabase = productRepository.findAll();
            redisTemplate.opsForValue().set(cacheKey, dataFromDatabase, timeout , timeUnit);
            return dataFromDatabase;
        }
    }

    public Optional<Product> getProductById(int id) {
        
         /*
           واکشی دیتا با id خاص از ردیس
           (اگر key با این id موجود بود نیاز به واکشی key کل product نباشد)
          */
        List<Product> cachedDataWithId = getCacheData(cacheKey+id);

        if (cachedDataWithId != null) {
            return cachedDataWithId.stream()
                    .filter(entity -> entity.getId().equals(id))
                    .findFirst();
        } else {
            List<Product> cachedData = getCacheData(cacheKey); // واکشی کل دیتای product
            if (cachedData != null) {
                return cachedData.stream()
                        .filter(entity -> entity.getId().equals(id))
                        .findFirst();
            } else {
                Optional<Product> entityFromDatabase = productRepository.findById(id);

                // ذخیره در ردیس با key خاص ( برای واکشی فیلد خاص نیاز به واکشی کل دیتای product نباشد)
                setToCache(entityFromDatabase, cacheKey+id);

                return entityFromDatabase;
            }
        }
    }

    /*
    ذخیره دیتا در ردیس
     */
    private void setToCache(Optional<Product> entityFromDatabase, String cacheKey) {
        entityFromDatabase.ifPresent(entity -> redisTemplate
                .opsForValue()
                .set(cacheKey, List.of(entity), timeout, timeUnit));
    }

    /*
    واکشی دیتا از ردیس
     */
    private List<Product> getCacheData(String cacheKey) {
        ObjectMapper objectMapper = new ObjectMapper(); // به دلیل خطای عدم تبدیل LinkedHashMap به Product
        return objectMapper.convertValue
                (redisTemplate.opsForValue().get(cacheKey) , new TypeReference<>() {});
    }

    public Optional<Product> getProductByName(String name) {
        List<Product> cachedData = getCacheData(cacheKey);
        if (cachedData != null) {
            return cachedData.stream()
                    .filter(entity -> entity.getName().equals(name))
                    .findFirst();
        } else {
            Optional<Product> entityFromDatabase = Optional.ofNullable(productRepository.findByName(name));
            entityFromDatabase.ifPresent(entity -> redisTemplate.opsForValue()
                    .set(cacheKey, List.of(entity), timeout, timeUnit));
            return entityFromDatabase;
        }
    }

    public String deleteProduct(int id) {
        List<Product> cachedData = getCacheData(cacheKey);
        if (cachedData != null) {
            cachedData.removeIf(entity -> entity.getId().equals(id));
            redisTemplate.opsForValue().set(cacheKey, cachedData, timeout, timeUnit);
        }
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }

    public Product updateProduct(Product product) {

        List<Product> cachedData = getCacheData(cacheKey);
        if (cachedData != null) {
            cachedData.replaceAll(entity -> entity.getId().equals(product.getId()) ? product : entity);
            redisTemplate.opsForValue().set(cacheKey, cachedData, timeout, timeUnit);
        }

        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        return productRepository.save(existingProduct);
    }
}
