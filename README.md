
# Orders & Inventory Service

Architecture Overview:

1. Two controllers created, OrderController,InventoryController to expose the API for CRUD operations

        POST /orders
        GET /orders/{id}
        POST /orders/{id}/confirm
        POST /orders/{id}/cancel
        GET /inventory/{productId}

For implementing logging, logback has been used

2. For storing the transactional data postgres has been used.


Design decisions:

Database Design 

Table: users

id: BIGSERIAL, Primary Key, NOT NULL
email: VARCHAR(255), NOT NULL, UNIQUE
display_name: VARCHAR(255), NOT NULL


Table: orders

id: BIGSERIAL, Primary Key, NOT NULL
user_id: BIGINT, NOT NULL, Foreign Key → users(id)
status: VARCHAR(32), NOT NULL (Enum: PENDING, CONFIRMED, CANCELLED)
created_at: TIMESTAMPTZ, NOT NULL
updated_at: TIMESTAMPTZ, NOT NULL


Table: inventory

id: BIGSERIAL, Primary Key, NOT NULL
product_id: VARCHAR(255), NOT NULL, UNIQUE
total_stock: INTEGER, NOT NULL, CHECK (total_stock >= 0)
reserved_stock: INTEGER, NOT NULL, CHECK (reserved_stock >= 0 AND reserved_stock <= total_stock)


Table: order_items

order_id: BIGINT, NOT NULL, Foreign Key → orders(id), ON DELETE CASCADE
product_id: VARCHAR(255), NOT NULL, (Optional FK → inventory(product_id))
quantity: INTEGER, NOT NULL, CHECK (quantity >= 1)
Primary Key: (order_id, product_id)


Relationships

users → orders: One-to-Many (user_id in orders references users.id)
orders → order_items: One-to-Many (order_id in order_items references orders.id)
inventory: Independent table; optionally linked to order_items.product_id


Concurrency strategy:
1. To make the create order API concurrent @EnableAsyc, CompletableFuture has been used in the code. 
2. To make sure that the system does not oversell we have implemented Pessimistic locking. This makes sure that whevever an order is getting place it
will make sure to lock the product until it is confirmed or cancelled.



Security approach:
To make sure the WRITE APIs remain secured,  UsernamePasswordAuthenticationFilter of spring security has been used to generate the JWT for accessing the APIs.


Start Application:

## Quick Start (one command)

```bash
docker compose up --build
```

App: http://localhost:8080

### Obtain JWT
```bash
curl -s -X POST http://localhost:8080/auth/login       -H "Content-Type: application/json"       -d '{"username":"demo@example.com","password":"password"}'
# Response: { "token": "Bearer eyJ..." }
```

### Use JWT for POST endpoints
```bash
TOKEN="Bearer <paste-token>"

# Create order
curl -X POST http://localhost:8080/orders       -H "Authorization: $TOKEN" -H "Content-Type: application/json"       -d '{
        "userId": 1,
        "items": [
          {"productId":"SKU-BOOK-123","quantity":2},
          {"productId":"SKU-MUG-456","quantity":1}
        ]
      }'

# Confirm / Cancel
curl -X POST http://localhost:8080/orders/1/confirm -H "Authorization: $TOKEN"
curl -X POST http://localhost:8080/orders/1/cancel  -H "Authorization: $TOKEN"

# Upsert inventory
curl -X POST "http://localhost:8080/inventory/SKU-BOOK-123?totalStock=20" -H "Authorization: $TOKEN"

# Public GET
curl http://localhost:8080/orders/1
curl http://localhost:8080/inventory/SKU-BOOK-123
```

## Import in Eclipse
1. **File → Import → Existing Maven Projects**
2. Select folder `orders-inventory-service/`
3. Ensure Project JDK is **Java 17**

## Tech
- Spring Boot 3.3.x, Java 17, Maven 3.8.8
- Spring Data JPA + PostgreSQL
- Pessimistic locking (`SELECT FOR UPDATE`) via JPA
- JWT auth with Spring Security (POST endpoints protected)

## Notes
- Demo users: `demo@example.com/password`, `admin@example.com/adminpass`
- Seed data: User id=1 and inventory rows created via `data.sql`
- In production, set a strong `APP_JWT_SECRET` via env/secret store.
