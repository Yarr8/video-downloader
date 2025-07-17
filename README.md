# Video Downloader Microservices

Service for downloading videos from different platforms (Reddit only supported for showcase).

## Stack

- Java 21 + Spring Boot
- Python + Flask
- PostgreSQL
- Docker / Docker Compose
- WebClient / REST
- Microservice architecture

## How to Run

###  Preparation

1. Create a `.env` file in the project root using `.env.example` as a template:

```bash
cp .env.example .env
```

2. Edit the file and provide your database credentials and service configuration.

---

###  Run with Docker Compose

```bash
docker compose up --build
```

This will start all services: `postgres`, `api-gateway`, `video-storage`, and `downloader-reddit`.

---

###  Run Services Manually (alternative)

#### api-gateway

```bash
cd api-gateway
./run.sh
```

This script runs the Spring Boot application via Maven. No environment variables are required.

---

#### video-storage

```bash
cd video-storage
./run.sh
```

This script loads environment variables from the shared `.env` file in the project root and runs the service using Maven.

---

#### downloader-reddit

```bash
cd downloader-reddit
# On Linux/macOS
./run.sh

# On Windows
run.bat
```

These scripts:
- Create and activate the virtual environment (if missing)
- Install dependencies
- Run `main.py`
- Load environment variables from the shared root `.env` file

---

## Request Example

curl "http://localhost:8080/api/videos?url=https://www.reddit.com/r/aww/comments/172bc65/kitty_learning_things_from_mom"
