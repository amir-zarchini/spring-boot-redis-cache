package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final static String cacheKey = "Product";
    private final static TimeUnit timeUnit = TimeUnit.MINUTES;
    private final static long timeout = 1;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public List<Product> getProducts() {
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        } else {
            List<Product> dataFromDatabase = productRepository.findAll();
            redisTemplate.opsForValue().set(cacheKey, dataFromDatabase, timeout , timeUnit);
            return dataFromDatabase;
        }
    }

    public Optional<Product> getProductById(int id) {
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData.stream()
                    .filter(entity -> entity.getId().equals(id))
                    .findFirst();
        } else {
            Optional<Product> entityFromDatabase = productRepository.findById(id);
            entityFromDatabase.ifPresent(entity -> redisTemplate
                    .opsForValue()
                    .set(cacheKey, List.of(entity), timeout, timeUnit));
            return entityFromDatabase;
        }
    }

    public Optional<Product> getProductByName(String name) {
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
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
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            cachedData.removeIf(entity -> entity.getId().equals(id));
            redisTemplate.opsForValue().set(cacheKey, cachedData, timeout, timeUnit);
        }
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }

    public Product updateProduct(Product product) {

        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
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
