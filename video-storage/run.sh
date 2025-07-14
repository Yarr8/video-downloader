#!/bin/bash

# Load environment variables from .env if it exists
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Run the Spring Boot application via Maven
mvn spring-boot:run
