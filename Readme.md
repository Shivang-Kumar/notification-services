# 📬 Notification Microservice

> A production-grade, event-driven notification system built with Spring Boot and Apache Kafka — supporting multi-channel delivery (Email, SMS) with retry logic, dead letter queues, and parallel processing.

## 📸 Screenshots

### Architecture
![Architecture](architecture.png)

### Kafka Topic — live messages
![Kafka](docs/images/kafka-topic.png)

### Notification status in PostgreSQL
![DB](docs/images/db-status.png)

---

## 📌 Why This Project?

In real-world systems — e-commerce, banking, SaaS — sending notifications is deceptively hard. You need **asynchronous processing**, **fault tolerance**, **retries**, and **channel extensibility** without losing a single message.

This microservice solves exactly that: notifications are never sent synchronously. Every event is persisted first, then processed reliably in the background — even across failures.

---

## ✨ Key Features

- **Fully asynchronous, event-driven** — built on Apache Kafka; no blocking calls
- **Multi-channel delivery** — Email (SMTP / SendGrid) and SMS, cleanly routed via a Channel Router
- **Retry mechanism** — failed notifications are retried multiple times automatically
- **Dead Letter Queue (DLQ)** — persistent failures are isolated without affecting healthy processing
- **Batch + parallel processing** — a scheduler pulls events in batches, a thread pool executes them concurrently
- **Template-based notifications** — dynamic content rendered with Thymeleaf
- **Status tracking** — every notification is tracked as `PENDING` → `SENT` / `FAILED`
- **Secure credential management** — SendGrid API key injected via environment variables, never hardcoded

---

## Architecture

This service follows an **event-driven, persistence-first** design. Notification requests are published to Kafka, persisted to PostgreSQL immediately, then processed asynchronously — ensuring zero data loss even if workers crash.



## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.x |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| Template Engine | Thymeleaf |
| Email | SMTP (local) / SendGrid (production) |
| SMS | Mock implementation (extensible) |
| Containerization | Docker |
| Build Tool | Maven |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven

### Run locally

# 1. Clone the repository
git clone https://github.com/Shivang-Kumar/notification-services.git
cd notification-services

# 2. Start Kafka, PostgreSQL via Docker Compose
docker-compose up -d

# 3. Run the application
./mvnw spring-boot:run

The service will start on `http://localhost:8080`.

---

## ⚙️ Configuration

All sensitive credentials are managed via environment variables — no secrets in the codebase.

```env
# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/notifications
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# SendGrid (optional — falls back to SMTP if not set)
SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_SENDER_EMAIL=your_verified_sender@example.com
```

> When `SENDGRID_API_KEY` is not set, the service falls back to SMTP for local/testing use.

---

##  How It Works — End to End

1. A **producer service** publishes a notification event to the `notification.event` Kafka topic.
2. The **Kafka consumer** receives the event, persists it to PostgreSQL as `PENDING`, and acknowledges the message.
3. A **scheduled worker** polls the database in batches and submits tasks to a **thread pool executor**.
4. The **Notification Processor** resolves the Thymeleaf template and passes the event to the **Channel Router**.
5. The Channel Router dispatches to the correct provider — Email or SMS.
6. On failure, the system **retries automatically**. After exhausting retries, the event is moved to the **Dead Letter Queue** and marked `FAILED`.

---

## 📂 Project Structure

src/
├── consumer/         # Kafka consumer — ingests and persists events
├── worker/           # Scheduler + thread pool executor
├── processor/        # Template resolution + channel routing logic
├── channel/          # Email and SMS provider implementations
├── model/            # Notification entity + status enums
├── repository/       # Spring Data JPA repositories
├── config/           # Kafka, thread pool, and app config
└── templates/        # Thymeleaf notification templates


---

## Notification Status Flow

PENDING  ──►  SENT
   │
   └──► (retry) ──► FAILED ──► DLQ

---

## Extending the System

Adding a new channel (e.g. Push Notification, Slack) requires only:

1. Implementing the `NotificationChannel` interface
2. Registering the channel in `ChannelRouter`

No changes to the core processing pipeline needed — the system is open for extension, closed for modification.
