package com.example.springbootrediscache.service;

import com.example.springbootrediscache.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product saveProduct(Product product);

    List<Product> saveProducts(List<Product> products);

    List<Product> getProducts();

    Optional<Product> getProductById(int id);

    Optional<Product> getProductByName(String name);

    String deleteProduct(int id);

    Product updateProduct(Product product);
}
