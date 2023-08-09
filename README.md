# Spring Boot Redis Cache Example

This repository provides an example of implementing Redis caching in a Spring Boot application. The project demonstrates how to use Spring's built-in cache abstraction with Redis as the caching provider.

## Table of Contents

- [Introduction](#introduction)
- [Setup](#setup)
- [Usage](#usage)
- [Caching Strategies](#caching-strategies)
- [Customization](#customization)
- [Contributing](#contributing)
- [License](#license)

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
`/productById/{id}`: Fetche a specific value by id. <br/>
`/product/{name}`: Fetche a specific value by name. <br/>
`/update`: update a product. <br/>
`/delete/{id}`: remove a product by id. <br/>
