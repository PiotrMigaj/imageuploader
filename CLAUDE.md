# Image Uploader - Project Overview for Claude Code

## Project Type & Technology Stack
- **Framework**: Quarkus (Java web framework) version 3.23.3
- **Language**: Java 21
- **Build Tool**: Maven
- **Architecture**: Hexagonal/Clean Architecture with Apache Camel integration
- **Frontend**: Single HTML page with Vue.js 3 and Bootstrap 5
- **Containerization**: Docker with JVM and native build options

## Core Purpose
An image uploader application for "Niebieskie Aparaty" (Blue Cameras) that allows authenticated users to upload images to AWS S3, with automatic compression from JPEG to WebP format, and metadata storage in DynamoDB.

## Project Structure

### Root Directory
```
/Users/piotrmigaj/Documents/development/personal/imageuploader/
├── pom.xml                    # Maven configuration
├── docker-compose.yaml       # Docker orchestration
├── README.md                  # Basic Quarkus setup instructions
├── src/
│   ├── main/
│   │   ├── java/pl/niebieskieaparaty/imageuploader/
│   │   ├── resources/
│   │   └── docker/            # Multiple Dockerfile variants
│   └── test/
└── target/                    # Build artifacts
```

### Main Source Structure (Hexagonal Architecture)
The application follows hexagonal architecture patterns with clear separation:

```
src/main/java/pl/niebieskieaparaty/imageuploader/
├── configuration/             # Application configuration
├── event/                     # Event management domain
│   ├── adapter/primary/rest/  # REST endpoints
│   ├── application/           # Application services
│   └── core/                  # Domain models
├── gallery/                   # Gallery management domain  
│   └── application/           # DynamoDB integration
├── security/                  # Authentication & authorization
│   └── adapter/primary/       # Login pages and logout
└── upload/                    # Core upload functionality
    ├── adapter/primary/       # REST API and MVC controllers
    ├── application/           # Business logic & processors
    └── core/                  # Domain objects (UploadedData)
```

## Key Features & Functionality

### 1. Authentication
- Form-based authentication using Quarkus Elytron
- Two hardcoded admin users configured via environment variables
- Session management with cookie-based authentication
- Login page: `/login`, protected routes: `/*`

### 2. Image Upload Pipeline
**Upload Flow**: Image → S3 (Original) + S3 (WebP Compressed) → DynamoDB → Event Update

**Key Components**:
- `UploadRestRoute`: REST API endpoint (`/api/uploads`)
- `UploadRoute`: Apache Camel routes for processing
- `JpegToWebpImageCompressor`: JPEG to WebP conversion using cwebp binary
- `S3PresignUrlProcessor`: Generates presigned URLs for uploaded files
- Parallel upload to both original and compressed buckets

### 3. Event Management
- Events can be retrieved via `/api/events`
- Events are updated with gallery information after successful uploads
- Integration with Camel for event processing

### 4. Gallery Management
- DynamoDB integration for storing upload metadata
- Tracks original and compressed file information

## Configuration

### Application Properties (`application.properties`)
Key configurations:
- **Authentication**: Form-based with embedded users
- **File Upload**: 20MB max body size
- **AWS Integration**: S3 and DynamoDB credentials
- **Security**: HTTP-only cookies, 1-hour session timeout

### Environment Variables Required
```bash
PIOTR_PASSWORD=<password>
ANNA_PASSWORD=<password>
AWS_ACCESS_KEY=<key>
AWS_SECRET_KEY=<secret>
AWS_REGION=<region>
AWS_BUCKET_NAME=<bucket>
BASE_BACKEND_PATH=<backend_path>
```

## Frontend Architecture

### Single Page Application
- **File**: `src/main/resources/templates/index.html`
- **Framework**: Vue.js 3 with Composition API
- **Styling**: Bootstrap 5 with custom CSS variables
- **Features**:
  - Event selection modal
  - Multi-file upload with progress tracking
  - Real-time upload progress per file and overall
  - Responsive design with modern UI

### Authentication UI
- **File**: `src/main/resources/templates/login.html`
- Clean, minimalist login form
- Error and logout message handling
- Consistent branding with main application

## Build & Deployment

### Development
```bash
./mvnw quarkus:dev          # Development mode with hot reload
```

### Production Builds
```bash
./mvnw package              # Standard JAR
./mvnw package -Dnative     # Native executable (GraalVM)
```

### Docker Support
- **JVM**: `src/main/docker/Dockerfile.jvm` (includes cwebp installation)
- **Native**: Multiple native Docker variants available
- **Compose**: `docker-compose.yaml` for orchestrated deployment

## Key Dependencies

### Core Framework
- `quarkus-rest`: REST API framework
- `quarkus-rest-jackson`: JSON serialization
- `quarkus-qute-web`: Template engine for HTML pages

### Integration
- `camel-quarkus-*`: Apache Camel for integration patterns
- `camel-quarkus-aws2-s3`: S3 integration
- `camel-quarkus-aws2-ddb`: DynamoDB integration

### Security
- `quarkus-elytron-security-properties-file`: Authentication

### Utilities
- `lombok`: Code generation
- `c-webp`: WebP image conversion library

## Architecture Patterns

### 1. Hexagonal Architecture
Clear separation between:
- **Adapters** (REST, MVC): External interfaces
- **Application** (Services, Processors): Business logic
- **Core** (Domain): Pure domain objects

### 2. Apache Camel Integration
- Route-based processing with clear separation of concerns
- Parallel processing for original and compressed uploads
- Aggregation strategy for combining results

### 3. Domain-Driven Design
Separate bounded contexts for:
- **Upload**: Core image upload functionality
- **Event**: Event management
- **Gallery**: Image gallery and metadata
- **Security**: Authentication and authorization

## Development Guidelines

### Code Organization
- Follow existing package structure with adapter/application/core layers
- Use Apache Camel routes for complex processing workflows
- Leverage Quarkus CDI for dependency injection
- Keep domain objects pure (see `UploadedData` record)

### Testing
- JUnit 5 and REST Assured configured
- Integration tests using `quarkus-junit5`

### Branching
- Main branch: `main`
- Current development: `dev/rea/selection-mover`
- Recent commits focus on Docker setup and base path configuration

## Important Notes for Claude Code

1. **Security**: The application uses embedded user authentication - be cautious when modifying security configuration
2. **AWS Integration**: All file operations use Apache Camel routes - maintain this pattern for consistency
3. **Image Processing**: The cwebp binary is installed in Docker - ensure this dependency is maintained
4. **Environment Variables**: The application requires multiple AWS-related environment variables to function
5. **Frontend**: Single HTML page with embedded Vue.js - modifications should maintain this architecture
6. **Architecture**: Maintain hexagonal architecture patterns when adding new features

## Common Tasks
- **Adding new upload processors**: Extend in `upload/application/processor/`
- **Modifying upload flow**: Update `UploadRoute.java` Camel routes
- **Frontend changes**: Modify `templates/index.html` Vue.js code
- **Authentication**: Configure in `application.properties`
- **New REST endpoints**: Follow existing pattern in `adapter/primary/rest/`

This project represents a modern Java microservice with clean architecture, cloud integration, and a responsive frontend, designed for efficient image upload and processing workflows.