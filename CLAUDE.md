# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./mvnw quarkus:dev              # Dev mode with hot reload (http://localhost:8080)
./mvnw package                  # Build JAR (output: target/quarkus-app/quarkus-run.jar)
./mvnw package -Dnative         # Build native executable (requires GraalVM)
./mvnw test                     # Run unit tests
./mvnw verify                   # Run unit + integration tests
./mvnw test -Dtest=ClassName    # Run a single test class
```

Docker: `docker-compose up --build` (uses `src/main/docker/Dockerfile.jvm`, requires `.env` file)

## Tech Stack

- **Quarkus 3.23.3** with Java 21, Maven, Lombok
- **Apache Camel** for all integration/processing pipelines (S3, DynamoDB, file operations)
- **AWS**: S3 (image storage), DynamoDB (metadata in `GalleriesCamel`, `Events`, `Selection` tables)
- **Frontend**: Vue.js 3 + Bootstrap 5 embedded in Qute HTML templates (`src/main/resources/templates/`)
- **Auth**: Quarkus Elytron form-based login with embedded users
- **Image processing**: cwebp binary (JPEG to WebP conversion, installed in Docker)

## Architecture

Hexagonal architecture with 6 bounded contexts: `upload`, `event`, `gallery`, `selection`, `file`, `security`.

Each domain follows: `adapter/primary/rest/` (REST endpoints) | `application/` (Camel routes + processors) | `core/` (domain records)

### Key Pattern: Apache Camel Routes

All business logic flows through Camel routes, not traditional service classes. Each domain defines:
- A `*Route.java` class with the Camel DSL route definitions
- A `*RouteApi.java` class with route URI constants (e.g., `DIRECT_UPLOAD_IMAGE_TO_BUCKETS = "direct:uploadImageToBuckets"`)
- `*Processor.java` classes that implement `Processor` for individual processing steps

REST endpoints (`*RestRoute.java`) receive HTTP requests and delegate to Camel routes via `ProducerTemplate.sendBodyAndHeaders()`.

### Upload Pipeline (core flow)

```
POST /api/uploads (multipart: eventId, username, file)
  → direct:uploadImageToBuckets
    → Multicast (parallel):
        ├─ direct:uploadImageToOriginalFolder → S3 upload → presigned URL
        └─ direct:uploadImageToCompressedFolder → JPEG→WebP compress → S3 upload → presigned URL
    → UploadAggregationStrategy merges results
    → Save to InMemoryImageUploadedDataRepository

POST /api/uploads/complete (JSON: eventId, imagesAmount)
  → Validates count matches uploaded files
  → For each file: save to DynamoDB (GalleriesCamel table)
  → Update Event with camelGallery=true flag
  → Clear in-memory data
```

The upload is a two-phase process: files upload individually to S3 (phase 1), then a completion call persists all metadata to DynamoDB at once (phase 2).

### S3 Key Structure

- Original: `{username}/{eventId}/images/original/{filename}`
- Compressed: `{username}/{eventId}/images/compressed/{filename}.webp`

### Selection Processing Flow

```
GET /api/selections?blocked=false → DynamoDB scan with filter → List<Selection>
POST /api/selections/process → Get selected image names from DynamoDB
  → Move matching .jpg files from source directory to {baseDir}/wybrane/ subfolder
```

**Camel file consumer gotcha:** `pollEnrich` with the file component moves consumed files to `.camel` by default (`noop=false`). Use `delete=true` instead when the intent is a file-move (consume + write to target) — this deletes the source file and prevents `.camel` from being created. See `FileRoute.java` `batchMoveImagesRoute`.

## Environment Variables

Required (see `.env` file): `PIOTR_PASSWORD`, `ANNA_PASSWORD`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION`, `AWS_BUCKET_NAME`, `BASE_BACKEND_PATH`

## Architectural Constraints

- All file/cloud operations must go through Apache Camel routes — do not use raw AWS SDK calls directly
- Domain objects in `core/` are Java records — keep them pure with no framework dependencies
- Frontend pages are single HTML files with embedded Vue.js — no build tooling or separate frontend project
- The `InMemoryImageUploadedDataRepository` holds upload state between the two-phase upload — it's intentionally not persisted
- DynamoDB clients are created per-domain in route configurations (not shared beans) using credentials from `application.properties`
