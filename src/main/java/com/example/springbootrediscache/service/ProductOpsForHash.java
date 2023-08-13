package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * this service wrote for config cache using RedisTemplate class
 * and opsForHash method
 */
@Service
@ConditionalOnProperty(name="service.choice",havingValue = "ProductOpsForHash")
public class ProductOpsForHash implements ProductService{

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductOpsForHash(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public List<Product> getProducts() {
        List<Product> cacheData = Collections.singletonList((Product) redisTemplate.opsForHash().values("product"));
        if (!cacheData.isEmpty()) return cacheData;
        else {
            return productRepository.findAll();
        }
    }

    public Optional<Product> getProductById(int id) {
        Product cacheData = (Product) redisTemplate.opsForHash().get("product", id);
        if (cacheData != null) return Optional.of(cacheData);
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }


    @Transactional
    public String deleteProduct(int id) {
        redisTemplate.opsForHash().delete("Product",id);
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }

    @Transactional
    public Product updateProduct(Product product) {
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        redisTemplate.opsForHash().put("Product" ,product.getId(),product);
        return productRepository.save(existingProduct);
    }
}
