<div align="center">
    <img src="src/main/resources/META-INF/resources/static/images/kamifusen-logo.png">
</div>

---

![GitHub Release](https://img.shields.io/github/v/release/tohuwabohu-io/kamifusen) ![Coverage](https://raw.githubusercontent.com/tohuwabohu-io/kamifusen/badges/jacoco.svg)

# kamifusen

> A privacy-focused page hit counter with analytics dashboard

Kamifusen is a simple yet powerful page hit counter written in Kotlin using the Quarkus framework. It tracks page visits by hashing user agent information to ensure unique visit counting while maintaining user privacy. Features include API key management, analytics dashboard, and session tracking.

## Features

- **Privacy-focused**: Hashes user agent info instead of storing personal data
- **Unique visit tracking**: Ensures each visitor is counted only once per page
- **Analytics dashboard**: Real-time statistics and visit trends
- **API key management**: Secure access control for multiple domains
- **Session tracking**: Advanced visitor journey analytics
- **Modern UI**: Terminal-style admin interface built with React
- **Scalable**: Built on Quarkus for cloud-native deployment

## Quick Start

### Prerequisites

- Java 17+
- PostgreSQL 15+
- Node.js 22+ (for UI development)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/tohuwabohu-io/kamifusen.git
   cd kamifusen
   ```

2. **Set up PostgreSQL database**
   ```bash
   # Create database and user
   createdb dev
   psql -d dev -c "CREATE USER kamifusen WITH PASSWORD 'kamifusen';"
   psql -d dev -c "GRANT ALL PRIVILEGES ON DATABASE dev TO kamifusen;"
   ```

3. **Configure environment (optional)**

   The application uses sensible defaults for development. You can override any configuration by setting environment variables or modifying `application.properties`:

   ```bash
   # Database (defaults shown)
   export QUARKUS_DATASOURCE_USERNAME=kamifusen
   export QUARKUS_DATASOURCE_PASSWORD=kamifusen
   export QUARKUS_DATASOURCE_REACTIVE_URL=postgresql://localhost:5432/dev

   # Auth (required for production)
   export QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY=your-32-char-encryption-key

   # CORS (optional)
   export QUARKUS_HTTP_CORS=true
   export QUARKUS_HTTP_CORS_ORIGINS=http://localhost:3000,https://yourdomain.com
   ```

4. **Run in development mode**
   ```bash
   ./gradlew quarkusDev
   ```

   This starts:
   - Backend server on http://localhost:8080
   - React dev server on http://localhost:3000 (proxy to backend)
   - Database migration (creates tables automatically)
   - Live reload for both backend and frontend

5. **First login**
   - Navigate to http://localhost:8080
   - Login with username: `admin`, password: `admin`
   - You'll be prompted to change the password on first login

### Production Configuration

Required environment variables for production:

```bash
# Database
QUARKUS_DATASOURCE_USERNAME=your_db_user
QUARKUS_DATASOURCE_PASSWORD=your_db_password
QUARKUS_DATASOURCE_REACTIVE_URL=postgresql://your_host:5432/your_db

# Security
QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY=your-32-character-encryption-key

# CORS (if serving from different domains)
QUARKUS_HTTP_CORS=true
QUARKUS_HTTP_CORS_ORIGINS=https://yourdomain.com
```

## API Usage

### Create API Key

1. Access the admin interface at your server URL
2. Navigate to "User Management"
3. Click "CREATE KEY" in the API Keys section
4. Copy the generated key (shown only once)

### Track Page Hits

Add this JavaScript to your pages:

```html
<script>
document.addEventListener('DOMContentLoaded', function () {
    const url = new URL(window.location.href);

    fetch('https://your-server.com/public/visits/hit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Basic YOUR_API_KEY_HERE'
        },
        body: JSON.stringify({
            path: url.pathname,
            domain: url.hostname
        })
    });
});
</script>
```

### Get Visit Count

```bash
curl -H "Authorization: Basic YOUR_API_KEY" \
     "https://your-server.com/public/visits/count/{pageId}"
```

Replace `{pageId}` with the UUID of the page you want to get visit counts for. Page IDs are available through the admin interface or by tracking the response from the hit endpoint.

## Building for Production

### Standard JAR
```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

### Native Executable
```bash
./gradlew build -Dquarkus.native.enabled=true
./build/*-runner
```

### Docker
```bash
docker build -f src/main/docker/Dockerfile.jvm -t kamifusen:jvm .
docker run -p 8080:8080 kamifusen:jvm
```

## Architecture

- **Backend**: Kotlin + Quarkus + PostgreSQL
- **Frontend**: React + TypeScript + Tailwind CSS
- **Build**: Gradle + Quinoa (integrates npm builds)
- **Database**: Flyway migrations
- **Auth**: Form-based authentication with secure sessions
- **Deployment**: Docker + GraalVM native compilation support

## Development

### UI Development
```bash
cd src/main/webui
npm run dev  # Separate dev server
```

### Generate API Client
```bash
./gradlew regenerateClient  # Updates TypeScript API client
```

### Testing
```bash
./gradlew test              # Backend tests
cd src/main/webui && npm test  # Frontend tests
```

## Learn More

- [Quarkus Framework](https://quarkus.io/)
- [Database Schema](src/main/resources/db/migration/)
- [API Documentation](spec/openapi.yaml)

