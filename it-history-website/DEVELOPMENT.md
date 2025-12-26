# IT History Website - Development Guide

## Quick Commands

### Windows PowerShell

**Start Backend:**
```powershell
cd D:\code\AICode\it-history-website\backend
mvn spring-boot:run
```

**Start Frontend:**
```powershell
cd D:\code\AICode\it-history-website\frontend
npm install  # First time only
npm run dev
```

**Access Application:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

## Project Structure

```
it-history-website/
├── backend/                          # Spring Boot backend
│   ├── src/main/java/com/ithistory/
│   │   ├── ItHistoryApplication.java        # Main application
│   │   ├── controller/
│   │   │   └── StoryController.java         # REST endpoints
│   │   ├── service/
│   │   │   └── StoryService.java            # Business logic
│   │   ├── repository/
│   │   │   ├── StoryRepository.java         # Story data access
│   │   │   ├── ImageRepository.java         # Image data access
│   │   │   └── ConfigurationRepository.java
│   │   ├── entity/
│   │   │   ├── Story.java                   # Story entity
│   │   │   ├── Image.java                   # Image entity
│   │   │   └── Configuration.java
│   │   ├── dto/
│   │   │   ├── StoryDto.java                # Story response
│   │   │   ├── ImageDto.java
│   │   │   ├── CalendarResponse.java
│   │   │   └── ErrorResponse.java
│   │   └── llm/
│   │       ├── LlmProvider.java             # Provider interface
│   │       ├── LlmException.java
│   │       ├── LlmRequest.java
│   │       ├── LlmResponse.java
│   │       └── impl/
│   │           └── OpenAiProvider.java      # OpenAI implementation
│   ├── src/main/resources/
│   │   └── application.properties           # Configuration
│   ├── pom.xml                              # Maven dependencies
│   └── .gitignore
│
├── frontend/                         # React frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── CalendarView.jsx             # Calendar component
│   │   │   ├── CalendarView.css
│   │   │   ├── StoryView.jsx                # Story display
│   │   │   └── StoryView.css
│   │   ├── App.jsx                          # Main app component
│   │   ├── App.css
│   │   ├── main.jsx                         # Entry point
│   │   └── index.css                        # Global styles
│   ├── index.html                           # HTML template
│   ├── package.json                         # NPM dependencies
│   ├── vite.config.js                       # Vite configuration
│   └── .gitignore
│
├── README.md                         # Main documentation
├── QUICKSTART.md                     # Quick start guide
├── IMPLEMENTATION_SUMMARY.md         # Implementation details
└── DEVELOPMENT.md                    # This file
```

## Development Workflow

### Initial Setup

1. **Install Prerequisites:**
   - Java 17+: https://adoptium.net/
   - Maven 3.6+: https://maven.apache.org/
   - Node.js 16+: https://nodejs.org/

2. **Clone/Setup Project:**
   ```powershell
   cd D:\code\AICode\it-history-website
   ```

3. **Backend Setup:**
   ```powershell
   cd backend
   mvn clean install
   ```

4. **Frontend Setup:**
   ```powershell
   cd frontend
   npm install
   ```

### Daily Development

**Start Both Servers:**

Terminal 1 (Backend):
```powershell
cd D:\code\AICode\it-history-website\backend
mvn spring-boot:run
```

Terminal 2 (Frontend):
```powershell
cd D:\code\AICode\it-history-website\frontend
npm run dev
```

**Hot Reload:**
- Backend: Spring Boot DevTools (auto-restart on save)
- Frontend: Vite HMR (instant updates)

### Making Changes

**Backend Changes:**
1. Edit Java files in `src/main/java/com/ithistory/`
2. Spring Boot auto-restarts on save
3. Test via Swagger UI or frontend

**Frontend Changes:**
1. Edit React files in `src/`
2. Vite hot-reloads automatically
3. View changes in browser instantly

**Database Changes:**
1. Modify entity classes
2. Spring JPA auto-updates schema (dev mode)
3. Verify in H2 Console

## Testing

### Backend Testing

**Run Unit Tests:**
```powershell
cd backend
mvn test
```

**Manual API Testing:**
```powershell
# Health check
curl http://localhost:8080/api/health

# Generate story
curl http://localhost:8080/api/story/1/1

# Get calendar
curl http://localhost:8080/api/calendar/2024/12
```

### Frontend Testing

**Browser Testing:**
1. Open http://localhost:3000
2. Test calendar navigation
3. Click dates to generate stories
4. Test back button
5. Resize browser for responsive testing

**Console Debugging:**
- Open browser DevTools (F12)
- Check Console for errors
- Monitor Network tab for API calls

## Configuration

### Backend Configuration

**`application.properties`:**

```properties
# Development settings
spring.jpa.show-sql=true              # Show SQL queries
logging.level.com.ithistory=DEBUG     # Debug logging

# Production settings
spring.jpa.show-sql=false
logging.level.com.ithistory=INFO
```

**Environment Variables:**
```powershell
# Set OpenAI API key (optional)
$env:OPENAI_API_KEY="sk-your-key"

# Set custom port
$env:SERVER_PORT="8090"
```

### Frontend Configuration

**`vite.config.js`:**

```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8080'  // Backend proxy
    }
  }
})
```

## Database Management

### H2 Console Access

1. Start backend
2. Open: http://localhost:8080/h2-console
3. Connect:
   - JDBC URL: `jdbc:h2:mem:ithistory`
   - Username: `sa`
   - Password: (empty)

### View Data

```sql
-- View all stories
SELECT * FROM STORIES;

-- View images
SELECT * FROM IMAGES;

-- View configurations
SELECT * FROM CONFIGURATIONS;

-- Count stories by month
SELECT DATE_MONTH, COUNT(*) 
FROM STORIES 
GROUP BY DATE_MONTH;
```

### Clear Cache

```sql
-- Delete all stories (clear cache)
DELETE FROM IMAGES;
DELETE FROM STORIES;
```

## Common Development Tasks

### Add New REST Endpoint

1. Add method in `StoryController.java`
2. Implement logic in `StoryService.java`
3. Test via Swagger UI
4. Update frontend to call new endpoint

### Add New Entity

1. Create entity class in `entity/` package
2. Create repository interface
3. Add service methods
4. Update controller if needed
5. JPA auto-creates table

### Add New React Component

1. Create `.jsx` file in `src/components/`
2. Create `.css` file for styles
3. Import and use in `App.jsx`
4. Test in browser

### Change LLM Provider

1. Implement `LlmProvider` interface
2. Add configuration properties
3. Update service to use new provider
4. Test story generation

## Debugging

### Backend Debugging

**Enable Debug Logging:**
```properties
logging.level.com.ithistory=DEBUG
logging.level.org.springframework.web=DEBUG
```

**Common Issues:**
- **Port in use**: Change `server.port` in properties
- **Database error**: Check H2 console for schema
- **LLM error**: Check API key and logs

### Frontend Debugging

**Browser DevTools:**
- Console: JavaScript errors
- Network: API call failures
- React DevTools: Component state

**Common Issues:**
- **Blank page**: Check console for errors
- **API fails**: Verify backend is running
- **Calendar broken**: Check react-calendar import

## Building for Production

### Backend

```powershell
cd backend
mvn clean package -DskipTests
```

Output: `target/it-history-backend-1.0.0.jar`

### Frontend

```powershell
cd frontend
npm run build
```

Output: `dist/` directory with static files

### Deploy

**Backend:**
```powershell
java -jar target/it-history-backend-1.0.0.jar
```

**Frontend:**
Serve `dist/` folder with any web server (nginx, Apache, etc.)

## Performance Tips

### Backend

- Enable connection pooling
- Use caching for repeated queries
- Optimize database indexes
- Monitor LLM API usage

### Frontend

- Lazy load images
- Minimize bundle size
- Use React.memo for expensive components
- Implement pagination for large datasets

## Git Workflow

```powershell
# Create feature branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "Add new feature"

# Push to remote
git push origin feature/new-feature

# Merge to main (after review)
git checkout main
git merge feature/new-feature
```

## Environment-Specific Settings

### Development
- H2 in-memory database
- Mock LLM responses (no API key needed)
- Verbose logging
- CORS允许 all origins

### Production
- PostgreSQL database
- Real LLM API
- Error-level logging
- Restricted CORS

## Monitoring

**Backend Health:**
```powershell
curl http://localhost:8080/api/health
```

**Database Size:**
```sql
SELECT COUNT(*) FROM STORIES;
SELECT COUNT(*) FROM IMAGES;
```

**API Response Times:**
Check backend logs for timing information.

## Helpful Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **React Docs**: https://react.dev/
- **Vite Docs**: https://vitejs.dev/
- **OpenAI API**: https://platform.openai.com/docs

## Support

For questions or issues:
1. Check QUICKSTART.md
2. Review IMPLEMENTATION_SUMMARY.md
3. Check backend logs
4. Check browser console
5. Review this development guide

Happy coding! 🚀
