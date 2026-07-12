# Car Dealership Inventory System

![Dashboard Preview](./dashboard-preview.png)

🔗 **[Live Demo Website](https://car-dealership-system.vercel.app/login)**

A production-quality full-stack Car Dealership Inventory System built with a Spring Boot REST backend, a MongoDB Atlas cloud database, and a responsive React Single Page Application (SPA) styled with custom glassmorphism Vanilla CSS. The project is built following Clean Architecture, SOLID design principles, and Test-Driven Development (TDD).

---

## 🛠️ Tech Stack & Requirements

### Backend REST API
*   **Language**: Java 17 (LTS)
*   **Framework**: Spring Boot 3.4.1
*   **Security**: Spring Security & JWT (Stateless Session)
*   **Persistence**: Spring Data MongoDB & MongoDB Atlas (Cloud)
*   **Utilities**: Lombok & Bean Validation (JSR-380)
*   **Testing**: JUnit 5, Mockito & MockMvc

### Frontend SPA
*   **Library**: React 19 (Vite environment)
*   **Environment Setup**: Vitest, JSDOM & React Testing Library (RTL)
*   **Styling**: Premium Glassmorphism & Vanilla CSS custom variables
*   **Routing**: React Router DOM (Client-side routing with vercel rewrite configuration)
*   **HTTP Client**: Axios (configured with Authorization headers injection)

---

## 📂 Folder Structure

The repository organizes backend source files and frontend assets separately:

```
car-dealership-system/
│
├── backend/                # Spring Boot backend project
│   ├── pom.xml             # Maven build configurations
│   ├── src/main/java/com/incubyte/backend/
│   │   ├── config/         # Security Config, CORS mapping, DataInitializer startup seeder
│   │   ├── controller/     # AuthController & VehicleController REST routes
│   │   ├── dto/            # Request DTOs (LoginRequest, RegisterRequest)
│   │   ├── exception/      # GlobalExceptionHandler mapping validations & system errors
│   │   ├── model/          # MongoDB entities (User, Vehicle)
│   │   ├── repository/     # Spring Data MongoDB Repository interfaces
│   │   ├── security/       # JwtAuthenticationFilter context encoder
│   │   └── service/        # Business service logics (AuthService, UserService, VehicleService, JwtService)
│   ├── src/main/resources/
│   │   └── application.properties # Server port binding, security secret key, and MongoDB Atlas settings
│   └── src/test/java/      # Integration and unit tests suites
│
├── frontend/               # React SPA Source Code (Vite setup)
│   ├── src/
│   │   ├── components/     # Login, Register, Dashboard views and CSS styling files
│   │   ├── App.jsx         # Router path configurations mapping to screens
│   │   ├── config.js       # Centralized API endpoint URL mappings
│   │   ├── index.css       # Global body & root layout styling
│   │   ├── main.jsx        # Mount point settings
│   │   └── setupTests.js   # Vitest environment setup importing jest-dom
│   ├── vercel.json         # SPA router rewrite rules
│   ├── package.json        # Node configurations and dependencies
│   └── vite.config.js      # Vite compilation assets and test configuration block
│
├── Dockerfile              # Multi-stage Dockerfile for backend Render deployment
└── README.md               # Project documentation
```

---

## 💾 Cloud Database Configuration

This project requires a connection to a MongoDB database. For cloud hosting, it utilizes **MongoDB Atlas**.

### Configuration
1.  Our backend is preconfigured to use the following environment variable for the connection string inside `backend/src/main/resources/application.properties`:
    ```properties
    spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/car-dealership}
    ```
2.  During deployment (e.g., on Render), make sure to configure the `MONGODB_URI` environment variable with your MongoDB Atlas connection string:
    ```
    mongodb+srv://<username>:<password>@<cluster-url>/car-dealership?retryWrites=true&w=majority
    ```

### Startup Data Initialization
At Spring Boot startup, the `DataInitializer` class automatically verifies if a default administrator exists. If not, it creates the following account in the database:
*   **Admin Username**: `admin@dealership.com`
*   **Admin Password**: `AdminPassword@123`
*   **Assigned Roles**: `ROLE_ADMIN`, `ROLE_USER`

---

## 🚀 Local Run Instructions

### 1. Starting the Spring Boot Backend Server
Open a terminal and navigate to the `backend` folder:
```bash
cd backend
```
Run tests to verify backend compilation:
```bash
mvn clean test
```
Start the Spring Boot REST server:
```bash
mvn spring-boot:run
```
The server will start running on port `8080` (http://localhost:8080).

### 2. Starting the React Vite Frontend Server
Open a new terminal and navigate to the `frontend` folder:
```bash
cd frontend
```
Install Node packages:
```bash
npm install
```
Run tests to verify frontend components:
```bash
npm run test
```
Start the development server locally:
```bash
npm run dev
```
The React application will start running on port `5173`. Open your web browser and navigate to:
http://localhost:5173

---

## 📡 API Endpoint Security Access Matrix

| HTTP Method | URI Path | Required Role | Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Permit All | Register a new user profile (defaults to `ROLE_USER`) |
| **POST** | `/api/auth/login` | Permit All | Login and obtain stateless Bearer JWT |
| **GET** | `/api/vehicles` | Authenticated | Retrieve lists of all available vehicles |
| **GET** | `/api/vehicles/search` | Authenticated | Query search (make, model, category, price range) |
| **GET** | `/api/vehicles/{id}` | Authenticated | Retrieve details of a single vehicle |
| **PUT** | `/api/vehicles/{id}` | Authenticated | Update properties of an existing vehicle |
| **POST** | `/api/vehicles` | Authenticated | Add a new vehicle to the inventory list |
| **DELETE** | `/api/vehicles/{id}` | **ROLE_ADMIN** | Delete a vehicle from the catalog |
| **POST** | `/api/vehicles/{id}/purchase` | Authenticated | Purchase a vehicle (decreases inventory stock) |
| **POST** | `/api/vehicles/{id}/restock` | **ROLE_ADMIN** | Restock vehicle inventory quantity |

---

## 🔒 Concurrency & Transaction Management

*   **Atomic Inventory Updates**: To prevent race conditions (such as multiple users purchasing the last item simultaneously and causing negative stock), the service logic evaluates stock levels and updates documents atomically at the database layer.
*   **Quantity Validations**: Validations are enforced in `VehicleService.java` to verify that requested quantities are strictly positive (`> 0`) before modifying the inventory status. If a purchase exceeds available stock, it throws an `IllegalArgumentException("Insufficient stock")` which returns a `400 Bad Request` payload containing the message.

---

## 🧪 Testing Report

This system is built using strict Test-Driven Development (TDD) cycle. We mock service dependencies and controller endpoints to isolate test environments.

### Running Tests
*   **Backend**: `mvn clean test`
*   **Frontend**: `npm run test`

### Coverage Summary (51 Tests Passed)
*   **Backend (39 JUnit Tests)**:
    *   `UserServiceTest` and `AuthenticationServiceTest`: Validate encryption encoding, email formats, password rules, and JWT generation.
    *   `VehicleServiceTest`: Validates CRUD behavior, regex searches, and quantity limits.
    *   `VehicleControllerTest` and `SecurityTest`: Confirm stateless security filters, CORS mappings, request validations, and role restrictions.
    *   `DataInitializerTest`: Verifies auto-seeding of the admin user.
*   **Frontend (12 Vitest Tests)**:
    *   `Login.test.jsx`: Confirms input elements, form submission parameters, and token storage in `localStorage`.
    *   `Register.test.jsx`: Confirms signup requests and successful registration redirection.
    *   `Dashboard.test.jsx`: Confirms grid rendering, dynamic search mapping, purchase handler updates, and admin feature gating.

---

# 🤖 AI Usage

## AI Tools Used

- ChatGPT
- Claude

## How AI Was Used

AI was used as a development assistant to accelerate implementation and reduce repetitive work. It helped with:

- Generating initial backend and frontend boilerplate
- Suggesting Spring Security and JWT configurations
- Assisting with React component structure
- Creating unit and integration test templates
- Providing debugging suggestions
- Helping prepare deployment configurations for Render and Vercel

## Manual Work

The following work was completed manually:

- Application architecture and project structure
- Business logic implementation
- MongoDB integration
- JWT authentication flow
- Role-based authorization
- API integration between frontend and backend
- Debugging and issue resolution
- Test execution and verification
- Deployment and production configuration

## Reflection

AI improved development speed by assisting with boilerplate code, repetitive tasks, and troubleshooting. All generated code was reviewed, modified where necessary, tested, and integrated into the project before being committed.

---

# 📦 Deployment

*   **Frontend**: [Vercel App](https://car-dealership-system.vercel.app/login)
*   **Backend**: [Render API Base](https://car-dealership-system-3h4q.onrender.com/api)
*   **Database**: MongoDB Atlas

---

# 📄 License

This project is developed for educational and assessment purposes.
