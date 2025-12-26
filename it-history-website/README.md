# IT History Website

An interactive web application that leverages large language models to narrate fascinating IT history stories. Users can explore significant events in computer history by selecting dates from an engaging calendar interface.

## 🌟 Features

- **Interactive Calendar**: Browse dates and discover IT history events
- **AI-Generated Stories**: Rich narratives about software releases, historical figures, protocol standards, and technological breakthroughs
- **Smart Caching**: Stories are cached to optimize performance and reduce API costs
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Image Integration**: Stories include relevant images with captions

## 🏗️ Architecture

### Frontend
- **React** with Vite for fast development
- **react-calendar** for interactive date selection
- **Axios** for API communication
- Responsive CSS with modern design

### Backend
- **Java 17** with Spring Boot 3.2.0
- **Spring Data JPA** for database operations
- **H2 Database** (development) / **PostgreSQL** (production)
- **OpenAI API** integration with provider abstraction layer
- **RESTful API** with OpenAPI documentation

## 📋 Prerequisites

### Backend
- Java 17 or higher
- Maven 3.6+

### Frontend
- Node.js 16+ and npm

### Optional
- OpenAI API key (for real story generation; mock responses work without it)

## 🚀 Quick Start

### Backend Setup

```powershell
# Navigate to backend directory
cd it-history-website\backend

# Build the project
mvn clean package

# Run the application (Windows)
java -jar target\it-history-backend-1.0.0.jar

# Or use Maven
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Optional**: Set OpenAI API key (if not set, mock responses will be used)
```powershell
$env:OPENAI_API_KEY="your-api-key-here"
mvn spring-boot:run
```

### Frontend Setup

```powershell
# Navigate to frontend directory
cd it-history-website\frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:3000`

## 🔧 Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# Database (H2 for development)
spring.datasource.url=jdbc:h2:mem:ithistory

# LLM Configuration
llm.openai.api-key=${OPENAI_API_KEY:}
llm.openai.model=gpt-3.5-turbo
llm.openai.max-tokens=2000
```

### Frontend Configuration
The frontend proxies API requests to the backend through Vite config.

## 📚 API Documentation

Once the backend is running, access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Key Endpoints

**Get Calendar Data**
```
GET /api/calendar/{year}/{month}
```

**Get Story**
```
GET /api/story/{month}/{day}?refresh=false
```

**Health Check**
```
GET /api/health
```

## 🗄️ Database

### H2 Console (Development)
Access the H2 console at: http://localhost:8080/h2-console

**JDBC URL**: `jdbc:h2:mem:ithistory`  
**Username**: `sa`  
**Password**: (empty)

### Production Database
For production, configure PostgreSQL in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ithistory
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 🎨 Usage

1. **Open the Application**: Navigate to http://localhost:3000
2. **Browse Calendar**: View the current month or navigate to other months
3. **Select a Date**: Click any date to trigger story generation
4. **Read Story**: Enjoy the AI-generated IT history narrative
5. **Explore More**: Return to calendar and discover other dates

Dates with cached stories are marked with a book icon (📖).

## 🔍 Development

### Backend Development

**Build**
```powershell
mvn clean package
```

**Run Tests**
```powershell
mvn test
```

**Check for Issues**
```powershell
mvn verify
```

### Frontend Development

**Build for Production**
```powershell
npm run build
```

**Preview Production Build**
```powershell
npm run preview
```

## 📁 Project Structure

```
it-history-website/
├── backend/
│   ├── src/main/java/com/ithistory/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access
│   │   ├── entity/          # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   └── llm/             # LLM provider abstraction
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── components/      # React components
    │   ├── App.jsx          # Main application
    │   └── main.jsx         # Entry point
    ├── package.json
    └── vite.config.js
```

## 🚢 Deployment

### Backend Deployment

**Create JAR**
```powershell
mvn clean package -DskipTests
```

**Run in Production**
```powershell
java -jar target/it-history-backend-1.0.0.jar --spring.profiles.active=prod
```

### Frontend Deployment

**Build**
```powershell
npm run build
```

The `dist/` directory contains static files ready for deployment to any web server.

## 🔒 Security Notes

- Never commit API keys to version control
- Use environment variables for sensitive configuration
- Enable CORS restrictions in production
- Implement rate limiting for public APIs

## 🐛 Troubleshooting

**Backend won't start**
- Verify Java 17 is installed: `java -version`
- Check if port 8080 is available
- Review logs in console output

**Frontend can't connect to backend**
- Ensure backend is running on port 8080
- Check browser console for errors
- Verify proxy configuration in vite.config.js

**No stories generating**
- Check if OpenAI API key is set (or use mock mode)
- Review backend logs for LLM errors
- Verify network connectivity

## 📝 License

This project is created for educational purposes.

## 🤝 Contributing

This is a demo project. Feel free to fork and customize for your needs.

## 📧 Support

For issues or questions, please refer to the design document or review the code comments.
