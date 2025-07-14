# Video Downloader Microservices

Service for downloading videos from different platforms (Reddit only supported for showcase).

## Stack

- Java 21 + Spring Boot
- Python + Flask
- PostgreSQL
- Docker / Docker Compose
- WebClient / REST
- Microservice architecture

## How to run

### Run with Docker
Coming soon: docker-compose.yml setup.

### Running services manually

#### api-gateway service

This section describes how to run the `api-gateway` service locally using Maven.
```bash
 cd .\api-gateway\
.\run.sh
```
This script run the Spring Boot application via Maven.
No environment variables are required for this service.

#### video-storage service

This section describes how to run the `video-storage` service locally using Maven and environment variables.

##### 1. Create `.env` file

Create a `.env` file in `video-storage/` directory, using `.env.example` as a template:

```bash
cp video-storage/.env.example video-storage/.env
```
Edit the file and provide your database credentials and configuration.

##### 2. Run the service

```bash
 cd .\video-storage\
.\run.sh
```

This script will automatically load environment variables and run the service via Maven.

#### downloader-reddit service

This section describes how to run the `downloader-reddit` service locally using Python.

##### 1. Create .env file
Copy the example file and edit values if needed:

```bash
cp .env.example .env
```
Example .env content:

```
PORT=5001
LOG_LEVEL=info
```

##### 2. Run the service
On Linux/macOS
```bash
 cd .\downloader-reddit\
.\run.sh
```

On Windows
```bash
 cd .\downloader-reddit\
.\run.bat
```

These scripts will:
- Create and activate the virtual environment (if missing)
- Install dependencies
- Run main.py

## Request Example

curl "http://localhost:8080/api/videos?url=https://www.reddit.com/r/aww/comments/172bc65/kitty_learning_things_from_mom"
