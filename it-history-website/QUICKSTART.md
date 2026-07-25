# IT History Website - Quick Start Guide

## Windows Quick Start

### Step 1: Start the Backend

```powershell
# Navigate to backend directory
cd D:\code\AICode\it-history-website\backend

# Run with Maven (recommended for development)
mvn spring-boot:run

# Alternative: Build and run JAR
mvn clean package
java -jar target\it-history-backend-1.0.0.jar
```

**Expected Output:**
```
Started ItHistoryApplication in X.XXX seconds
```

Backend is now running at: **http://localhost:8080**

### Step 2: Start the Frontend

Open a **new PowerShell window**:

```powershell
# Navigate to frontend directory
cd D:\code\AICode\it-history-website\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Expected Output:**
```
VITE ready in XXX ms
Local: http://localhost:3000
```

Frontend is now running at: **http://localhost:3000**

### Step 3: Use the Application

1. Open browser: **http://localhost:3000**
2. Click any date on the calendar
3. Wait for AI to generate the story (may take 10-20 seconds on first request)
4. Read the IT history story
5. Click "Back to Calendar" to explore other dates

## API Testing

**Test backend directly:**

```powershell
# Health check
curl http://localhost:8080/api/health

# Get story for January 15
curl http://localhost:8080/api/story/1/15

# Get calendar for December 2024
curl http://localhost:8080/api/calendar/2024/12
```

## Using OpenAI API (Optional)

By default, the app uses **mock responses**. To use real OpenAI:

**Windows PowerShell:**
```powershell
# Set API key in current session
$env:OPENAI_API_KEY="sk-your-actual-api-key"

# Run backend
cd D:\code\AICode\it-history-website\backend
mvn spring-boot:run
```

**Persistent configuration (recommended):**

Edit `backend/src/main/resources/application.properties`:
```properties
llm.openai.api-key=sk-your-actual-api-key
```

⚠️ **Never commit API keys to git!**

## Image Generation Configuration (Optional)

The application can generate AI images for stories using Kie.ai Nano Banana Pro API:

**Windows PowerShell:**
```powershell
# Set API key in current session
$env:KIE_AI_API_KEY="your-kie-ai-api-key"

# Run backend
cd D:\code\AICode\it-history-website\backend
mvn spring-boot:run
```

**Persistent configuration:**

Edit `backend/src/main/resources/application.properties`:
```properties
image.kie-ai.api-key=your-kie-ai-api-key
```

**Image Storage Location:**
- Default: `%USERPROFILE%\it-history-images`
- Images are organized by date (e.g., `2024-01-15/story_01-15_abc123.png`)
- Configure custom path in `application.properties`:
  ```properties
  image.storage.base-path=D:\MyImages\it-history
  ```

**Get Kie.ai API Key:**
1. Visit: https://kie.ai/api-key
2. Sign up for an account
3. Generate an API key
4. Cost: ~$0.12 per 2K image

## Stopping the Application

**Backend:**
- Press `Ctrl+C` in the backend terminal

**Frontend:**
- Press `Ctrl+C` in the frontend terminal

## Troubleshooting

### Port Already in Use

**Backend (8080):**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID)
taskkill /PID <PID> /F
```

**Frontend (3000):**
```powershell
# Find process using port 3000
netstat -ano | findstr :3000

# Kill process (replace PID)
taskkill /PID <PID> /F
```

### Java Not Found

```powershell
# Check Java version
java -version

# Should show Java 17 or higher
```

If not installed, download from: https://adoptium.net/

### Node/NPM Not Found

```powershell
# Check Node version
node -version

# Check npm version
npm -version
```

If not installed, download from: https://nodejs.org/

### Maven Not Found

**Use Maven wrapper (included):**
```powershell
.\mvnw spring-boot:run
```

Or install Maven from: https://maven.apache.org/

## Quick Commands Reference

### Backend Commands

| Command | Description |
|---------|-------------|
| `mvn spring-boot:run` | Start backend server |
| `mvn clean package` | Build JAR file |
| `mvn test` | Run tests |
| `mvn clean` | Clean build artifacts |

### Frontend Commands

| Command | Description |
|---------|-------------|
| `npm install` | Install dependencies |
| `npm run dev` | Start dev server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |

## Database Access

**H2 Console (Development):**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:ithistory`
- Username: `sa`
- Password: (empty)

## API Documentation

**Swagger UI:**
- URL: http://localhost:8080/swagger-ui.html

**API Docs JSON:**
- URL: http://localhost:8080/api-docs

## Default Configuration

| Setting | Value |
|---------|-------|
| Backend Port | 8080 |
| Frontend Port | 3000 |
| Database | H2 (in-memory) |
| LLM Mode | Mock (no API key needed) |
| Log Level | INFO |

## Next Steps

1. ✅ Verify both servers are running
2. ✅ Access http://localhost:3000
3. ✅ Generate your first story
4. 📖 Read the full README.md for advanced features
5. 🔧 Customize configuration as needed

## Getting Help

- Check the main README.md for detailed documentation
- Review application logs for error messages
- Verify all prerequisites are installed
- Ensure both backend and frontend are running

Happy exploring IT history! 🚀
