# Twitter Clone Project
##### This project is a simple Twitter-like clone built using Java, Javalin web framework, Jdbi for database interactions, Guice for dependency injection, Flyway for database migrations, and PostgreSQL as the database. The front end is rendered using Pebble templates.
```bash
/src
|-- /main
|   |-- /java
|   |   |-- /org/example
|   |   |   |-- /controllers
|   |   |   |-- /dao
|   |   |   |-- /models
|   |   |   |-- /services
|   |   |-- /application.conf
|   |-- /resources
|       |-- /templates
|       |   |-- /partials
|       |-- /db
|           |-- /migration
|
|-- /test
    |-- /java
            |-- UserServiceTest.java
            |-- TweetServiceTest.java
            |-- TestModule.java
```

## Running Instructions:
### Prerequisites:
Java 11+
Maven
Docker
PostgreSQL 
Setup Database:

Create a PostgreSQL database named TwitterDB.
Configure database credentials in application.conf (for local development).
## Flyway Migration:

Migrate the database using Flyway.
```bash
mvn flyway:migrate
```
## Run the Application:

To start the application:
```bash
mvn clean compile exec:java
```

## Testing with Testcontainers:

The tests use Testcontainers to spin up a PostgreSQL instance in a Docker container.
```bash
mvn test
```
This will execute the unit and integration  using Selenide.

## video 
https://drive.google.com/file/d/14-VCPmgf_71g3x_4bg1oZpbjllmsPw28/view?usp=drive_link
    
