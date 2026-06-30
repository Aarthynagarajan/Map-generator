# Installation & Local Setup Guide

Follow these steps to set up and run ProcessPro locally.

## Prerequisites

Ensure you have the following installed:
- **Java 21 JDK**
- **Node.js 20+**
- **Maven 3.9+**
- **Docker & Docker Compose**

---

## Local Development Setup

### 1. Database Setup
Start a local PostgreSQL instance and create a database named `processpro`:
```sql
CREATE DATABASE processpro;
```

### 2. Backend Configurations
Copy the env file and set your key values:
```bash
cp .env.example .env
```
Ensure you provide a valid `OPENAI_API_KEY` for AI features to execute.

Run Flyway migrations and start the backend:
```bash
cd backend
mvn spring-boot:run
```
The backend will run on port `8080`.

### 3. Frontend Setup
Navigate to the frontend folder, install dependencies, and start Vite:
```bash
cd frontend
npm install
npm run dev
```
The frontend will run on `http://localhost:5173`.

---

## Run Tests

### Backend Tests
```bash
cd backend
mvn clean test
```

### Frontend Typechecking
```bash
cd frontend
npm run typecheck
```
