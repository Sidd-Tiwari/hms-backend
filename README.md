<<<<<<< HEAD
=======
# Hotel Management CMS - MySQL Production Starter

A production-ready starter for a Hotel Management System CMS/admin panel using:

- Backend: Java 21, Spring Boot 3.x, Spring Security, JWT, Spring Data JPA, Flyway
- Database: MySQL 8
- Frontend: React + TypeScript + Vite + Tailwind CSS
- Deployment: Docker Compose ready

## Default Admin Login

```txt
Email: admin@hotel.com
Password: Admin@123
```

Change it immediately in production.

## Run with Docker

```bash
cp .env.example .env
docker compose up --build
```

Services:

```txt
Frontend: http://localhost:5173
Backend API: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
MySQL: localhost:3306
```

## Run Backend Locally

```bash
docker compose up -d mysql
cd backend
mvn spring-boot:run
```

## Run Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

## Included Modules

- JWT Login
- Admin dashboard
- Hotels
- Room types
- Rooms
- Customers
- Bookings
- Payments
- CMS Pages
- MySQL normalized schema
- Flyway migration
- Docker Compose
>>>>>>> fa922eddd8a76b43c4e9a6501739820c32a4dada
"# hms-backend" 
