# Product Service - Testing Guide

## Overview
This document explains the testing strategy for the Product Service, including the types of tests, how to run them, and the rationale behind the setup. It also covers the test pyramid, mocking strategy, and troubleshooting tips for Spring Boot tests.

---

## Test Types

### 1. Unit Tests (Web Layer)
- **Location:** `src/test/java/com/services/productservice/ProductControllerWebMvcTest.java`
- **Tooling:** `@WebMvcTest`, `MockMvc`, `@MockBean` for service and transaction manager
- **Scope:** Tests the `ProductController` in isolation, mocking the service layer. No database, Elasticsearch, or Redis is started.
- **Why:** Fast feedback, verifies controller logic and request/response mapping.

### 2. Integration Tests
- **Location:** `src/test/java/com/services/productservice/ProductControllerIntegrationTest.java`
- **Tooling:** `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `TestRestTemplate`, `@MockBean` for all repositories and infrastructure
- **Scope:** Boots the full Spring context, tests the controller and service wiring, but mocks out DB, Elasticsearch, Redis, and cache.
- **Why:** Ensures wiring and configuration work as expected, but remains fast and reliable by mocking infrastructure.

### 3. Context Load Test
- **Location:** `src/test/java/com/services/productservice/ProductserviceApplicationTests.java`
- **Tooling:** `@SpringBootTest`, `@MockBean` for all required beans
- **Scope:** Verifies that the Spring context loads with all necessary mocks in place.
- **Why:** Catches configuration or bean wiring issues early.

---

## The Test Pyramid

```
     ^
     |         E2E/UI
     |        (not in this module)
     |
     |    Integration Tests
     |----------------------
     |   Unit (Web) Tests
     |----------------------
     | Context Load Test
     +----------------------->
```
- **Base:** Context load test ensures the app can start.
- **Middle:** Unit (web) tests are fast, focused, and mock all dependencies except the controller.
- **Top:** Integration tests boot the full context but mock infrastructure for speed and reliability.
- **E2E/UI:** Not included here, but would sit at the top of the pyramid in a full system.

---

## Mocking Strategy
- **Unit tests:** Mock the service layer, only test controller logic.
- **Integration tests:** Mock all repositories and infrastructure (DB, ES, Redis, CacheService) to avoid external dependencies.
- **Context test:** Mock everything required to start the context.
- **Why:** Keeps tests fast, reliable, and focused on the code under test.

---

## Special Spring Boot Test Configuration
- **application.properties** in `src/test/resources` excludes security and Redis auto-configurations to avoid context load errors.
- **SecurityConfig** is excluded from tests using `@Profile("!test")` and `spring.profiles.active=test`.
- **PlatformTransactionManager** is mocked in web tests to satisfy `@Transactional` on controller methods.

---

## How to Run Tests

From the `productservice` directory:

```
mvn test
```

Or to run a specific test class:

```
mvn -Dtest=ProductControllerWebMvcTest test
```

---

## Thought Process & Troubleshooting
- **Align tests with actual controller endpoints and HTTP methods.**
- **Update tests to match controller response codes (e.g., 204 for PATCH/DELETE).**
- **Mock all infrastructure in tests to avoid external dependencies.**
- **If you see context load errors, check for missing mocks, missing test properties, or auto-configurations that need to be excluded.**
- **If you see 405 errors, check that your test uses the correct HTTP method and endpoint as defined in the controller.**

---

## Summary
- The test setup follows the test pyramid: most tests are fast and focused, with a few integration tests for wiring, and a single context load test for configuration.
- All tests are aligned with the actual controller implementation and HTTP contract.
- The setup is robust against common Spring Boot test pitfalls. 