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
./video-storage/run.sh
```

This script will automatically load environment variables and run the service via Maven.


## Request Example

curl "http://localhost:8080/api/videos?url=https://www.reddit.com/r/aww/comments/172bc65/kitty_learning_things_from_mom"