# FastFruit Webshop

A full e-commerce web app for a smoothie and juice shop, built with Spring Boot. It covers the whole shopping flow, from browsing products to paying with PayPal, plus an admin panel for managing the shop.

<!-- Add screenshots here:
![Home page](docs/home.png)
![Admin panel](docs/admin.png)
-->

## Features

**For customers**
- Product catalogue with categories
- Shopping cart
- Checkout with **PayPal** (sandbox) or cash on delivery
- Order history and order status tracking (pending, paid, shipped, delivered...)
- Registration, login, profile editing and password change

**For admins**
- Managing products, categories, orders and users
- Login logs and active session overview

**Security**
- Spring Security with role-based access (user / admin)
- Custom filters for request logging and security headers

## Tech stack

| Area | Technologies |
|------|--------------|
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA, MapStruct, Lombok |
| Frontend | Thymeleaf, HTML, CSS |
| Payments | PayPal Checkout SDK |
| Database | MySQL (local), PostgreSQL (production) |
| Deployment | Docker, Render |

## Running locally

1. Create a MySQL database called `webshop`.
2. Set your database credentials and PayPal sandbox keys in `webshop/src/main/resources/application.properties`.
3. Run:
   ```bash
   cd webshop
   ./mvnw spring-boot:run
   ```
4. Open `http://localhost:8080`.

**With Docker**
```bash
cd webshop
docker build -t fastfruit .
docker run -p 8080:8080 fastfruit
```

## Project structure

```
webshop/src/main/java/hr/java/web/webshop/
├── controller/   # web and admin controllers, cart API, PayPal checkout
├── service/      # business logic
├── repository/   # Spring Data JPA repositories
├── model/        # entities
├── dto/ mapper/  # DTOs and MapStruct mappers
├── filter/       # logging and security header filters
└── config/       # PayPal and web configuration
```
