# Lingke Todo

A minimal Todo app built with Spring Boot 3 + Thymeleaf + MySQL.

## Prerequisites

- Java 17+
- MySQL 8.x
- Maven 3.8+

## Setup

1. Create database:
```sql
CREATE DATABASE lingke_todo;
```

2. Update `src/main/resources/application.yml` with your MySQL credentials.

3. Run:
```bash
mvn spring-boot:run
```

4. Open http://localhost:8080

## Test
```bash
mvn test
```
