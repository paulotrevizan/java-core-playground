# java-core-playground

This is a study project focused on **Java Core** and **Spring Boot**, with focus on:
- Language fundamentals
- Best practices
- Clean architecture

## Stack
- Java 21
- Spring Boot 4.0.1
- Spring Web
- Spring Data JPA
- H2 Database
- JUnit 5 + AssertJ

## Project Goals
- Reinforce Java Collections, Streams, and Immutability
- Understand exceptions (checked vs unchecked)
- Apply unit and integration testing
- Build a simple and well-structured REST API

## Project Structure
```
src/
├── main/
│   └── java/
│       └── com/trevizan/javacoreplayground/
│           ├── controller
│           ├── service
│           ├── repository
│           ├── model
│           └── exception
└── test/
    └── java/
        └── com/trevizan/javacoreplayground/
            ├── controller
            ├── service
            └── repository
```

## Running the Project
Run the Spring Boot application:
```
./mvnw spring-boot:run
```

## Running Tests
Execute unit and integration tests:
```
./mvnw test
```

## Notes
This project is designed for **learning and experimentation purposes only**.  
The main focus is to reinforce core Java concepts and best practices and will be **gradually developed** over time.
