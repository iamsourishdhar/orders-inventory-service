Co-Pilot has been used in step by step manner to create this microservice logically. Please find the main promts used to build the component.
1. Build a Spring Boot microservice with Java 17 and Maven 3.8.8 that manages Orders and Inventory with the following characteristics:
   1. Core Capabilities
      2. Order Lifecycle
      3. Create an order with multiple items
      4. Reserve inventory atomically
      5. Confirm or cancel an order
      6. Prevent overselling under concurrent requests
      7. Inventory Management
      8. Track available and reserved stock
      9. Handle concurrent order placement safely
      10. Release inventory on order cancellation or timeout
      11. Failure Handling
      12. If inventory reservation fails, order creation must fail cleanly
      13. No partial or inconsistent states
      14. Functional Requirements
          APIs
          Implement REST APIs for in two controllers i.e OrderController, InventoryController:
          POST /orders
          GET /orders/{id}
          POST /orders/{id}/confirm
          POST /orders/{id}/cancel
          GET /inventory/{productId}
          Use proper HTTP semantics and error handling.
          Create Data Layer with the JPA classes. Create three Model classes, User, Order, Inventory. User is one to many mapped to Order schema. And Order is on to many mapped to Inventory.
          Use PostgreSQL for DB connection setup
          Make sure implement concurrency and consistency using Pessimistic Locking mechanism to avoid race condition and overselling.
2. Provide the ready to run  project in a zipped format which can be imported in Eclipse
3. Add a Dockerfile with docker-compose.yml for app + DB. And the app must run in one command.
4. Add Mockito Junit test cases for the orders-inventory-service 
5. Add JWT authentication using spring security for POST endpoints of OrderController.java, InventoryController.java on top of it
6. Add logging for orders-inventory-service using logback.
7. Create DB schema diagram based on the following JDO classes.
8. Implement concurrency using completable future, @EnableAsync in orders-inventory-service project for the OrderController.create API

After generating the code using Copilot, the code has been validated locally. Few fixes required to be done in pom.xml, controller layer to make sure the build is successful.
