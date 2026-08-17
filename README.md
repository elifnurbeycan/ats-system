# ATS System

Role-based applicant tracking and recruitment process management backend.

The system enables HR teams to centrally track candidates contacted through
LinkedIn or other channels, manage recruitment processes, record interactions
and interviews, and protect sensitive candidate information through
role-based permissions.

## Technology Stack

- Java 21
- Spring Boot 3.5.16
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- JWT
- Maven

## Requirements

Before running the project, install:

- JDK 21
- PostgreSQL 17
- Maven 3.9+
- Git

## Database Setup

Create a PostgreSQL database from `template0`. This avoids encoding errors on
PostgreSQL installations whose default `template1` database uses `SQL_ASCII`:

```sql
CREATE DATABASE ats_system
    WITH
    OWNER = postgres
    TEMPLATE = template0
    ENCODING = 'UTF8';
```

PowerShell alternative:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" `
  -h localhost -p 5432 -U postgres `
  -T template0 -E UTF8 ats_system
```

Restore the anonymized development database included in the repository:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" `
  -h localhost -p 5432 -U postgres `
  -d ats_system --no-owner --no-privileges -v `
  ".\database\ats_system_seed.dump"
```

Default local ports are frontend `3000`, backend `8080`, and PostgreSQL `5432`.

## Local Configuration

Copy the example development configuration:

```powershell
Copy-Item `
  src/main/resources/application-dev.example.yaml `
  src/main/resources/application-dev.yaml
```

Open `application-dev.yaml` and replace:

```yaml
password: CHANGE_ME
```

with your local PostgreSQL password.

`application-dev.yaml` contains local credentials and must not be committed.

## Running the Application

Activate the `dev` profile:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternatively, configure the following environment variable in IntelliJ:

```text
SPRING_PROFILES_ACTIVE=dev
```

## Running Tests

```powershell
mvn clean test -Dspring.profiles.active=dev
```

## Configuration Profiles

- `application.yaml`: Shared application configuration
- `application-dev.yaml`: Local development credentials; ignored by Git
- `application-dev.example.yaml`: Example configuration committed to Git
- Production secrets must be supplied through environment variables or a
  secret-management service.

## Commit Convention

This project follows Conventional Commits:

```text
feat: add a new feature
fix: correct an application error
refactor: restructure code without changing behavior
test: add or update tests
docs: update documentation
build: update dependencies or build configuration
chore: perform maintenance work
```

## Project Status

The project is currently under active development.
