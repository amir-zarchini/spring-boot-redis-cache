package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

//    @Resource(name="redisTemplate")
//    private HashOperations<String, Integer, Product> hashOperations;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

//    @Cacheable()
//    @CollectionCacheable
    public List<Product> getProducts() {

        String cacheKey = "Product";
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        } else {
            List<Product> dataFromDatabase = productRepository.findAll();
            redisTemplate.opsForValue().set(cacheKey, dataFromDatabase);
            return dataFromDatabase;
        }

//        template.opsForHash().values(hashReference);
//        hashOperations.entries(hashReference);
//        RedisTemplate.opsForList().leftPushAll()
//        return productRepository.findAll();
    }

//    @Override
//    @Cacheable(value = "usersList", key = "#page")
//    public List<User> allUsers(Integer page) {
//        Page<User> users = userRepo.findAll(PageRequest.of(--page, 5));
//        if (users.isEmpty()) {
//            throw new CustomException("Users not found", 400);
//        }
//        return users.getContent();
//    }

//    @Cacheable(value="Product", key="#id")
    public Optional<Product> getProductById(int id) {
        String cacheKey = "Product";
        List<Product> cachedData = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData.stream()
                    .filter(entity -> entity.getId().equals(id))
                    .findFirst();
        } else {
            Optional<Product> entityFromDatabase = productRepository.findById(id);
            entityFromDatabase.ifPresent(entity -> redisTemplate.opsForValue().set(cacheKey, List.of(entity)));
            return entityFromDatabase;
        }
//        return productRepository.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @CacheEvict(value="Product", key="#id")
    public String deleteProduct(int id) {
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }

    @CachePut(value="Product", key="#product.id", condition="#product.id!=null")
    public Product updateProduct(Product product) {
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
//        hashOperations.put(hashReference, product.getId(), product);
        return productRepository.save(existingProduct);
    }

}
