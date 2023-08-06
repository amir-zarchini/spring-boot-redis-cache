package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

//    @Resource(name="redisTemplate")
//    private HashOperations<String, Integer, Product> hashOperations;

    private final RedisTemplate template;

    private final String hashReference= "Product";
    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

//    @Cacheable(value = "Product")
    public List<Product> getProducts() {
        template.opsForHash().values(hashReference);
//        hashOperations.entries(hashReference);
        return productRepository.findAll();
    }

    @Cacheable(value="Product", key="#id")
    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @CacheEvict(value="Product", key="#id")
    public String deleteProduct(int id) {
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }

    @CachePut(value="Product", key="#id", condition="#id!=null")
    public Product updateProduct(Product product) {
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
//        hashOperations.put(hashReference, product.getId(), product);
        return productRepository.save(existingProduct);
    }

}
