package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * this service wrote for config cache using RedisTemplate class
 * and opsForHash method
 */
@Service
@RequiredArgsConstructor
public class ProductOpsForHash {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public List<Product> getProducts() {
        List<Product> productList = productRepository.findAll();
        Collection<Object> ProductsId = fillProductsId(productList);
        redisTemplate.opsForHash().multiGet("Products", ProductsId);
        return productRepository.findAll();
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
