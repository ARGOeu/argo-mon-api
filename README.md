# ARGO Mon API

The **ARGO Mon API** is part of the **ARGO Monitoring Service**, developed and maintained by **GRNET**.  
It provides REST endpoints for managing **tenants**, **projects**, **status pages**, monitoring resources, and automation workflows, integrating with ARGO Monitoring services and data sources.

The API allows authenticated users to manage tenants, projects, **status pages**, groups, and monitoring configuration, while relying on **Keycloak** for authentication and access control.

---

## Overview

The ARGO Mon API enables:

- Management of **Status Pages** (create, list, update, delete)
- Retrieval of **Status Groups** and **Reports** from ARGO Monitoring
- Configuration of **Public Status Pages** for external visibility
- Management of **User Profiles** linked with Keycloak identities

---

## Project Structure

This project is a **REST API** built with **Quarkus** and **Maven**, following a modular architecture and the **service–repository pattern**.

### Modules

1. **api**  
   Exposes REST endpoints for client interaction.

2. **dto**  
   Defines Data Transfer Objects used between API and service layers.

3. **entity**  
   Contains ORM entities representing database tables.

4. **enum**  
   Contains enumeration definitions used across the API.

5. **exception**  
   Defines custom exceptions used throughout the application.

6. **handler**  
   Centralizes error handling and exception mapping.

7. **mapper**  
   Uses **MapStruct** for entity-to-DTO conversions.

8. **repository**  
   Provides data access logic and database queries.

9. **service**  
   Contains the business logic of the API.

10. **util**  
    Contains utility classes and helpers such as configuration utilities.

---

## Authentication

All protected resources require authentication through **Keycloak**.  
Clients must include a **Bearer token** in each request to access restricted endpoints.

### Access the Protected Resources

Since the API’s endpoints must only be accessible to verified clients, every client who wants to access the API must be authenticated.  
Communication with protected endpoints is performed using **Bearer Authentication** — a token-based authentication method commonly used in HTTP APIs.

Include the access token in your HTTP requests using the **Authorization** header:

```
Authorization: Bearer {{token}}
```

---

## Access Token Retrieval

The [ARGO Mon API](https://api-status.devel.mon.argo.grnet.gr/) allows users to obtain an access token for authentication purposes.  
By following these steps, users can retrieve an access token to authenticate API requests.

### Instructions

1. **Open the Web Page**  
   Navigate to the [ARGO Mon API](https://api-status.devel.mon.argo.grnet.gr/) page.

2. **Locate the Access Token Button**  
   Once the page loads, locate the button that retrieves the access token.

3. **Click the “Obtain an Access Token” Button**  
   Clicking this button initiates the retrieval process from the authentication server.

4. **Provide Required Information**  
   Choose your preferred identity provider and log in with your credentials.

5. **Retrieve the Access Token**  
   After successful authentication, the token will appear on the page.

6. **Use the Access Token**  
   Include it in your API requests as described above.

---

# Instructions for Developers

## Prerequisites

Ensure the following software is installed on your development environment:

- **Java Development Kit (JDK)** 17
- **Apache Maven** 3.9
- **Docker**

---

## Install Dependencies

After cloning the repository, navigate to the root directory and execute:

```bash
mvn clean install -DskipTests=true -U
```

This command installs all required dependencies in your local Maven repository.

---
## Start Argo Web API in Development Profile

In development mode, Argo Mon API uses a locally hosted Argo Web API instead of the production service. To start the local Argo Web API instance, follow these instructions:

1. **Clone the ARGO Web API repository**

Clone the repository and check out the `devel` branch:

```bash
git clone -b devel https://github.com/argoeu/argo-web-api

Clone the repository and check out the `devel` branch:

```bash
git clone -b devel https://github.com/argoeu/argo-web-api
```
3. 
4. **Start the local Argo Web API using Docker Compose**
Run the following command to start the services:

```bash
docker compose up
```

The local instance of the Argo Web API will then be accessible at:
```text
http://localhost:8843
```


## Start the Application with Quarkus Dev Services

### Running the API

The development environment uses **Quarkus Compose Dev Services** to automatically start the required Keycloak container.

Simply run:

```bash
mvn clean quarkus:dev
```

On the first startup, Quarkus will automatically start the local Keycloak instance using the Docker Compose configuration located under:

```text
keycloak/docker-compose.yml
```

No manual `docker-compose up` is required.

Once the services are running, Keycloak is available at:

```text
http://localhost:58080
```

The API is available at:

```text
http://localhost:8080
```

> **Note**
>
> Ensure the local Argo Web API is running before starting the API.


## Welcome Page

The application provides a welcome page containing two actions:

### Visit API Documentation

Redirects you to the OpenAPI/Swagger documentation where you can explore:

* The REST endpoints provided by the API.
* The endpoints exposed by the `quarkus-auth` extension.

### Obtain an Access Token

Redirects you to a page that helps you obtain an access token from Keycloak for testing secured endpoints.

## Available Users

The following test users are pre-configured:

| Username | Password | Role                |
| -------- | -------- | ------------------- |
| admin    | admin    | Super Administrator |
| user1    | user1    | Standard User       |
| user2    | user2    | Standard User       |
| user3    | user3    | Standard User       |

Use these credentials to authenticate through Keycloak and test the available APIs.

## Keycloak Account / Group Management (RCIAM)

To access the RCIAM group management / account console as an admin, use the following URL:

```text
http://localhost:58080/realms/rciam/account
```

Login with:

* Username: `user1`
* Password: `user1`

---

## Running Tests Locally

> ⚠️ **Important**
>
> Make sure Docker is running before executing the tests.
>
> If you have manually started a local Keycloak instance, stop it first to avoid port conflicts with Quarkus Dev Services.


## Access the Dev Service Database

Connect to the automatically started PostgreSQL instance using:

```bash
psql -h localhost -U status -d status
```

> Default development password: `status`

Or connect directly inside the running container:
```bash
docker exec -it <container_id> psql -U status -d status
```

---

## GitHub Packages

ARGO Mon API depends on the `quarkus-auth` library, which is distributed through GitHub Packages.

Although the package is publicly available, GitHub Packages requires authentication when downloading Maven dependencies.

### 1. Create a GitHub Personal Access Token

Create a Personal Access Token at:

https://github.com/settings/tokens

The token must include the following permission:

- `read:packages`

If you also need access to private repositories, include:

- `repo`

### 2. Configure Maven

Create or edit the following file:

```text
~/.m2/settings.xml
```

Add the following configuration:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

Replace `YOUR_GITHUB_USERNAME` and `YOUR_GITHUB_PERSONAL_ACCESS_TOKEN` with your GitHub credentials.

### 3. Build the Project

```bash
mvn clean install
```

## License & Credits

ARGO Mon API is a service developed and maintained by **GRNET**.  
Distributed under the **Apache 2.0 License**.


