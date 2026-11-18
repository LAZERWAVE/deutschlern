# DeutschLern Backend

DeutschLern is a backend for managing German vocabulary and recording student learning activity. It is implemented as two Spring Boot services: a learning service for vocabulary and event publishing, and a progress service for consuming learning events and reporting student word counts.

## Features

- Create, update, retrieve, search, and delete German vocabulary entries.
- Store each word's article, plural form, English and Indonesian translations, and CEFR level.
- Support bulk vocabulary creation, statistics, and level analytics.
- Search vocabulary through PostgreSQL or Elasticsearch, with optional level and article filters.
- Publish `word-learned` events to Kafka when a student learns a word.
- Persist learning activity in PostgreSQL and cache progress counts in Redis.
- Run the services and their dependencies using Docker Compose.

## Architecture

```text
Client
  |                          +---------------------+
  +-- vocabulary / search -->| Learning Service   |-- PostgreSQL (vocabulary)
  |                          |       :8081         |-- Elasticsearch (search index)
  |                          |                     |-- Kafka: word-learned
  |                          +---------------------+
  |                                             |
  |                          word-learned event |
  |                                             v
  |                          +---------------------+
  +-- progress query ------>| Progress Service   |-- PostgreSQL (activities)
                             |       :8082         |-- Redis (progress cache)
                             +---------------------+
```

The learning service publishes an event when `/api/v1/learning-activities/learn` is called. The progress service consumes that event, stores a learning activity, and invalidates that student's cached word count. A later progress request loads the count from PostgreSQL and caches it in Redis for 10 minutes. The progress service also configures a Kafka Streams topology that reads `word-learned`, aggregates keyed counts, and writes to `student-word-counts`.

PostgreSQL is the source of truth for vocabulary and learning activities. Elasticsearch holds a vocabulary search index; vocabulary create, update, delete, and bulk-create operations also synchronize documents to that index. The rebuild endpoint copies all vocabulary records from PostgreSQL into Elasticsearch.

## Technology

- Java 25
- Spring Boot 4.1.2
- Spring MVC, Validation, Spring Data JPA, Spring Kafka, Kafka Streams, Spring Data Redis, and Spring Data Elasticsearch
- PostgreSQL 16
- Apache Kafka 4.1.1
- Redis 7
- Elasticsearch 9.4.8
- Maven Wrapper
- Docker Compose

## Repository layout

```text
.
├── docker-compose.yml
├── learning-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── progress-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
```

### Learning Service

Runs on port `8081`. Owns the vocabulary API, PostgreSQL vocabulary repository, Elasticsearch indexing/search, and Kafka event producer. Main packages include `controller`, `service`, `repository`, `entity`, `dto`, and `messaging`.

### Progress Service

Runs on port `8082`. Consumes learning events, persists activity records, invalidates cached student counts, and serves progress queries. Main packages include `controller`, `service`, `repository`, `entity`, and `messaging`.

## Requirements

For local development, install:

- Docker Desktop with Docker Compose
- Java 25 JDK (for running with Maven outside Docker)

Docker builds each service using its bundled Maven Wrapper and Java 25 container images. You do not need a separately installed Maven when building inside Docker.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

The first run downloads the container images and builds both Java services. When the containers are ready, the service URLs are:

| Component | Local address |
| --- | --- |
| Learning Service | `http://localhost:8081` |
| Progress Service | `http://localhost:8082` |
| PostgreSQL | `localhost:5432` |
| Kafka external listener | `localhost:9094` |
| Redis | `localhost:6379` |
| Elasticsearch | `http://localhost:9200` |

The Compose configuration creates persistent named volumes for PostgreSQL, Kafka, Redis, and Elasticsearch data. Stop the containers while retaining data with:

```bash
docker compose down
```

To remove the containers and their persisted development data:

```bash
docker compose down --volumes
```

## Run services directly

Start the dependencies first:

```bash
docker compose up -d postgres kafka redis elasticsearch
```

Then, in separate terminals, run each service from its own directory:

```bash
cd learning-service
./mvnw spring-boot:run
```

```bash
cd progress-service
./mvnw spring-boot:run
```

On Windows Command Prompt or PowerShell, use `mvnw.cmd` in place of `./mvnw`:

```powershell
cd learning-service
.\mvnw.cmd spring-boot:run
```

Both services use `localhost` dependency addresses by default when run directly. Their database settings are in `src/main/resources/application.properties`; Docker Compose overrides the connection hosts to use the Compose service names.

## API reference

All application endpoints use the `/api/v1` prefix. Request and response bodies are JSON.

### Vocabulary

Base path: `/api/v1/vocabulary` (Learning Service, port `8081`)

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/vocabulary` | List all vocabulary entries |
| `GET` | `/api/v1/vocabulary/{id}` | Get one entry by ID |
| `GET` | `/api/v1/vocabulary/level/{level}` | Filter by CEFR level (`A1` through `C2`) |
| `GET` | `/api/v1/vocabulary/article/{article}` | Filter by article (`DER`, `DIE`, or `DAS`) |
| `GET` | `/api/v1/vocabulary/search?keyword=...&page=0&size=10` | Case-insensitive German-word search, paged and sorted by word |
| `GET` | `/api/v1/vocabulary/statistics` | Total vocabulary count and counts by level and article |
| `POST` | `/api/v1/vocabulary` | Create one vocabulary entry (`201 Created`) |
| `POST` | `/api/v1/vocabulary/bulk` | Create multiple entries (`201 Created`) |
| `PUT` | `/api/v1/vocabulary/{id}` | Replace an existing entry |
| `DELETE` | `/api/v1/vocabulary/{id}` | Delete an entry (`204 No Content`) |
| `GET` | `/api/v1/vocabulary/analytics/levels` | Return level analytics |
| `GET` | `/api/v1/vocabulary/analytics/levels/comparison` | Return level comparison analytics |
| `POST` | `/api/v1/vocabulary/search/rebuild-index` | Rebuild Elasticsearch from PostgreSQL |

Example create request:

```json
{
  "germanWord": "Apfel",
  "article": "DER",
  "pluralForm": "Äpfel",
  "englishTranslation": "apple",
  "indonesianTranslation": "apel",
  "level": "A1"
}
```

`germanWord`, `englishTranslation`, and `indonesianTranslation` must be non-blank. The German word is limited to 100 characters; translations and plural form are limited to 150 and 100 characters respectively. `article` and `level` are required enum values. The API trims the German word on create and update. Duplicate words return a conflict response.

### Elasticsearch vocabulary search

The Learning Service exposes a separate Elasticsearch-backed search endpoint:

```http
GET /api/v1/vocabulary/search/elastic?keyword=Apfel&level=A1&article=DER&page=0&size=10
```

`keyword` is required. `level`, `article`, `page`, and `size` are optional. This search returns indexed vocabulary documents and supports level and article filters.

### Learning activity event submission

Base path: `/api/v1/learning-activities` (Learning Service, port `8081`)

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/learning-activities/learn` | Publish a learning event to Kafka (`202 Accepted`) |

Example request:

```json
{
  "studentId": 101,
  "vocabularyId": 1,
  "germanWord": "Apfel",
  "level": "A1"
}
```

The response indicates that the event was submitted to Kafka. Activity persistence occurs asynchronously in the progress service after consumption.

### Progress

Base path: `/api/v1/progress` (Progress Service, port `8082`)

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/progress/{studentId}/word-count` | Return the student's recorded learning activity count |

Example:

```bash
curl http://localhost:8082/api/v1/progress/101/word-count
```

The endpoint returns a numeric count. A newly submitted learning event may take a short time to be consumed and reflected in the count.

## Error responses

The Learning Service centralizes several errors through a REST exception handler:

- `400 Bad Request` for invalid request data or pagination arguments.
- `404 Not Found` when a requested vocabulary entry does not exist.
- `409 Conflict` for duplicate words or database uniqueness conflicts.

Validation errors include a message identifying the first invalid field. Error responses use JSON fields such as `status`, `error`, and `message` (and, for some handlers, `timestamp`).

## Tests

Each service has its own Maven project and test suite. Run tests from each service directory:

```bash
cd learning-service
./mvnw test
```

```bash
cd progress-service
./mvnw test
```

On Windows, use `mvnw.cmd test`. The Dockerfiles currently package the applications with tests skipped (`-DskipTests`); run the commands above to execute the test suites explicitly.

## Configuration

The local defaults are defined in each service's `src/main/resources/application.properties`. Compose supplies container-specific environment overrides for database, Kafka, Elasticsearch, and Redis addresses.

| Setting | Local default | Purpose |
| --- | --- | --- |
| PostgreSQL database | `deutschlern` | Vocabulary and activity persistence |
| PostgreSQL username | `deutschlern` | Local development database user |
| PostgreSQL password | `deutschlern_dev` | Local development database password |
| Kafka bootstrap server | `localhost:9094` | Event producer and consumer broker connection |
| Elasticsearch | `http://localhost:9200` | Vocabulary search index |
| Redis | `localhost:6379` | Cached progress counts |

The Compose credentials are intended for local development. Change them before using this setup in any shared or production environment.

## Data and behavior notes

- JPA schema management is configured with `spring.jpa.hibernate.ddl-auto=update` in both services.
- Vocabulary records have a unique German word constraint and indexes for level and article.
- Redis progress entries use keys of the form `progress:{studentId}` with a 10-minute time to live. A consumed learning event deletes that student's cached entry.
- Elasticsearch is a derived search index. Use the rebuild endpoint to repopulate it from the PostgreSQL vocabulary table.
- Kafka's external listener is available at `localhost:9094`; containers connect to the internal listener at `kafka:9092`.

## Troubleshooting

**A service cannot connect to a dependency:** Confirm the dependency container is running with `docker compose ps`, then inspect logs with `docker compose logs <service>` (for example, `docker compose logs kafka`).

**A port is already in use:** Stop the other process or update the corresponding host port mapping in `docker-compose.yml`.

**Elasticsearch takes time to start:** Allow the container to initialize, then check `http://localhost:9200` and `docker compose logs elasticsearch` before starting requests that depend on search.

**Progress does not change immediately:** Event submission is asynchronous. Wait for Kafka consumption, then repeat the progress request. The consumer invalidates the Redis cache when it records an event.

**Search results are missing:** Confirm Elasticsearch is running and rebuild the index with `POST /api/v1/vocabulary/search/rebuild-index`.

## Assessment context

This repository contains an implementation of a Java backend assessment project. The README documents the implementation that is present in the repository, including its endpoints, service boundaries, dependencies, and run commands. Assessment requirements should be checked against the supplied assessment brief separately; this README does not claim that every requirement has been independently verified.
