package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * this service wrote for config cache using RedisTemplate class
 * and opsForHash method
 */
@Service
public class ProductOpsForHash {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOperations;

    public ProductOpsForHash(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public List<Product> getProducts() {
//        List<Product> productList = productRepository.findAll();
//        Collection<Object> ProductsId = fillProductsId(productList);
//        redisTemplate.opsForHash().multiGet("Products", ProductsId);
        Map<String, Object> productMap = hashOperations.entries("products");
        if (!productMap.isEmpty()) {
            return productMap.values().stream()
                    .map(obj -> (Product) obj)
                    .collect(Collectors.toList());
        } else {
            List<Product> dataFromDatabase = productRepository.findAll();

            productMap = (Map<String, Object>) dataFromDatabase;
//            productMap = dataFromDatabase.stream().collect(Collectors.toMap());
            return dataFromDatabase;
        }
    }

    private Collection<Object> fillProductsId(List<Product> productList) {
        Collection<Object> productId = new ArrayList<>();
        productList.forEach(product -> {
            productId.add(product.getId().toString());
        });
        return productId;
    }


    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }


    public String deleteProduct(int id) {
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }


    public Product updateProduct(Product product) {
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        redisTemplate.opsForHash().put("Products" ,product.getId(),product);
        return productRepository.save(existingProduct);
    }
}
