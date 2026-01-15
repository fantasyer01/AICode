# AI Portfolio - Run Instructions

## Prerequisites

- **Node.js** 18+ (for frontend)
- **Java 21** (for backend)
- **Maven** (for backend build)
- **MySQL** running on localhost:3306

## Backend Setup

### 1. Database Configuration

Ensure MySQL is running with the following credentials (or update `backend/src/main/resources/application-dev.yml`):

- Host: `localhost:3306`
- Username: `root`
- Password: `root_password`
- Database: `portfolio` (auto-created)

### 2. Build and Run

```bash
cd backend

# Build the project
mvn clean install

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 3. Verify Backend

- API Base: http://localhost:8080/api/projects
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

## Frontend Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Environment Configuration

The `.env.local` file is pre-configured with:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. Run Development Server

```bash
npm run dev
```

### 4. Access the Application

Open http://localhost:3000 in your browser.

## Quick Start (Both Services)

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | Get all projects |
| GET | `/api/projects?category=ai` | Get projects by category |
| GET | `/api/projects/{id}` | Get project by ID |

**Categories:** `web`, `mobile`, `ai`, `data`, `all`

## Troubleshooting

### CORS Errors
The backend is configured to allow requests from `localhost:3000` and `localhost:3001`. If using a different port, update `backend/src/main/resources/application-dev.yml`.

### Database Connection
If MySQL connection fails, verify:
1. MySQL service is running
2. Credentials match `application-dev.yml`
3. Port 3306 is available

### Frontend Cannot Connect to Backend
1. Ensure backend is running on port 8080
2. Check `.env.local` has correct `NEXT_PUBLIC_API_URL`
3. Restart frontend after changing environment variables
