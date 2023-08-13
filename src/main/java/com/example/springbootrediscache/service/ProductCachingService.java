package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import com.example.springbootrediscache.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * cached config by cache annotation
 */
@Service
@AllArgsConstructor
@ConditionalOnProperty(name="service.choice",havingValue = "ProductCachingService")
public class ProductCachingService implements ProductService{

    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    /*
    ساخت دیتابیس با کلید []Product::SimpleKey
     */
    @Cacheable(value = "Product")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    /*
    key --> Product::#id جستجو با
     */
    @Cacheable(value="Product", key="#id")
    public Optional<Product> getProductById(int id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    /*
    پاک کردن redis برای key --> Products
    به این شکل بعد از آپدیت مقدار درون ردیس با کلید Products پاک میشود
    و با فراخوانی مجدد متد getProducts
    یک کش جدید از دیتا با این کلید برای دیتابیس دوباره ساخته میشود
    کش مربوط به کلید Product(با id مربوطه) پاک میشود
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "Product", key = "#id"),
            @CacheEvict(cacheNames = "Products", allEntries = true)
    })

    /*
    key --> Product::#id پاک کردن
     */
    @CacheEvict(value="Product", key="#id")
    public String deleteProduct(int id) {
        productRepository.deleteById(id);
        return "product removed id: " + id;
    }


    /*
    پاک کردن redis برای key Products
    به این شکل بعد از آپدیت مقدار درون ردیس با کلید Products پاک میشود
    و با فراخوانی مجدد متد getProducts
    یک کش با این کلید برای دیتابیس دوباره ساخته میشود
    کش مربوط به کلید Product(با id مربوطه) آپدیت میشود
     */
    @Caching(put = @CachePut(cacheNames = "Product", key = "#result.id"),
            evict = @CacheEvict(cacheNames = "Products", allEntries = true))

    /*
    پاک کردن redis برای key Products
    به این شکل بعد از آپدیت مقدار درون ردیس پاک میشود
     و با فراخوانی مجدد متد getProducts
    یک کش جدید از دیتا برای دیتابیس دوباره ساخته میشود
     */
    @CacheEvict(value = "Products", allEntries = true)

    /*
    key --> Product::#id اپدیت
     */
    @CachePut(value="Product", key="#product.id")

    public Product updateProduct(Product product) {
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        return productRepository.save(existingProduct);
    }
}
