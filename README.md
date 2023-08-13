# Spring Boot Redis Cache Example

This repository provides an example of implementing Redis caching in a Spring Boot application. The project demonstrates how to use Spring's built-in cache abstraction with Redis as the caching provider.

## Table of Contents

- [Introduction](#introduction)
- [Setup](#setup)
- [Usage](#usage)
- [Getting Started](#getting-started)

## Introduction

This Spring Boot project showcases how to integrate Redis caching into your application. Caching can significantly improve the performance of your application by reducing database hits and speeding up data retrieval for frequently accessed data.

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/amir-zarchini/spring-boot-redis-cache.git

2. Navigate to the project directory:
   ```bash
   cd spring-boot-redis-cache
   
3. Build and run the application:
   ```bash
   ./mvnw spring-boot:run

4. Access the application at `http://localhost:8080`.

## Usage

This application provides endpoints for demonstrating caching behavior. The following endpoints are available:

`/addProduct`: save product. <br/>
`/addProducts`: save products. <br/>
`/products`: Fetches all values. <br/>
`/productById/{id}`: Fetch a specific value by id. <br/>
`/product/{name}`: Fetch a specific value by name. <br/>
`/update`: update a product. <br/>
`/delete/{id}`: remove a product by id. <br/>

## Getting Started

###Maven Dependency

```xml
<dependencies>
   <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>

   <dependency>
      <groupId>redis.clients</groupId>
      <artifactId>jedis</artifactId>
   </dependency>

   <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
   </dependency>
</dependencies>
```

### Redis Configuration

```java
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.
                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> productRedisTemplate = new RedisTemplate<>();
        productRedisTemplate.setConnectionFactory(redisConnectionFactory);
        productRedisTemplate.setKeySerializer(new StringRedisSerializer());
        productRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
        productRedisTemplate.afterPropertiesSet();
        return productRedisTemplate;
    }
}
```

### Spring ProductService
Spring Boot Product Service Implementation will be like below class. I used 3 way for caching

1. used by spring boot cache annotation:

```java
@Service
@AllArgsConstructor
public class ProductCachingService {

    private final ProductRepository productRepository;

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
    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
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
```

2. used by opsForValue() method of RedisTemplate class:

```java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final static String cacheKey = "Product";
    private final static TimeUnit timeUnit = TimeUnit.MINUTES;
    private final static long timeout = 10;

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
                setToCache(entityFromDatabase, cacheKey+ "::" +id);

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

```

3. used by opsForHash() method of RedisTemplate class:

```java
@Service
public class ProductOpsForHash {

   private final ProductRepository productRepository;
   private final RedisTemplate<String, Object> redisTemplate;

   public ProductOpsForHash(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository) {
      this.redisTemplate = redisTemplate;
      this.productRepository = productRepository;
   }

   public List<Product> getProducts() {
      List<Product> cacheData = Collections.singletonList((Product) redisTemplate.opsForHash().values("product"));
      if (!cacheData.isEmpty()) return cacheData;
      else {
         return productRepository.findAll();
      }
   }

   public Product getProductById(int id) {
      Product cacheData = (Product) redisTemplate.opsForHash().get("product", id);
      if (cacheData != null) return cacheData;
      return productRepository.findById(id).orElse(null);
   }

   public Product getProductByName(String name) {
      return productRepository.findByName(name);
   }


   @Transactional
   public String deleteProduct(int id) {
      redisTemplate.opsForHash().delete("Product", id);
      productRepository.deleteById(id);
      return "product removed id: " + id;
   }

   @Transactional
   public Product updateProduct(Product product) {
      Product existingProduct = productRepository.findById(product.getId()).orElse(null);
      existingProduct.setName(product.getName());
      existingProduct.setQuantity(product.getQuantity());
      existingProduct.setPrice(product.getPrice());
      redisTemplate.opsForHash().put("Product", product.getId(), product);
      return productRepository.save(existingProduct);
   }
}
```

### application.properties

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/rediscachetest
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update

spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.redis.cache-null-values=true
```

