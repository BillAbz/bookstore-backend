# Bookstore – Full-Stack Application

A full-stack bookstore application developed as an educational project for the **Application Architecture** course at **Erasmus Hogeschool Brussel**.

The application offers a public book catalogue, account registration and login, book reservations, reviews, and role-based administration. It integrates with the **Open Library API** to import books using an ISBN.

## Technology stack

| Backend | Frontend | Tools |
| --- | --- | --- |
| Java 21, Spring Boot 4, Spring Web MVC | React, JavaScript, Vite | Maven, npm, GitHub Actions |
| Spring Security, OAuth2 Resource Server, JWT | HTML, CSS | JUnit 5, Mockito, Postman |
| Spring Data JPA, Hibernate, H2 | Fetch API | Open Library REST API |

## Features

- **Catalogue:** browse books, view price and stock, and read public reviews.
- **Accounts:** register, log in, and log out. Passwords are stored as BCrypt hashes.
- **JWT authentication:** a successful login returns a Bearer token valid for one hour.
- **Reservations:** authenticated users can reserve available books, view their own reservations, and cancel uncollected reservations. Reserving reduces stock; cancellation restores it.
- **Reservation expiration:** a scheduled task checks for uncollected reservations older than 24 hours and releases their stock.
- **Collection:** administrators can mark an active reservation as collected. Collected reservations cannot be cancelled or expired automatically.
- **Reviews:** users can submit a rating (1–5) and comment. Only one review per user and book is permitted. Users can delete their own reviews.
- **Administration:** administrators can create, update, and delete books, mark reservations as collected, and inspect reservations for a book.
- **Open Library integration:** administrators can import book metadata by ISBN; an educational pricing strategy calculates the initial price from the page count.

## Project structure

```text
bookstore/
├── backend/                 # Spring Boot API and application
│   ├── src/main/java/       # Controllers, services, repositories, entities, DTOs
│   ├── src/main/resources/  # Application configuration and built frontend files
│   ├── src/test/java/       # Backend tests
│   ├── pom.xml
│   └── mvnw
├── frontend/                # React source code
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── .github/workflows/       # CI pipeline
├── build.sh                 # Full-stack build script
└── README.md
```

## Architecture and design patterns

The backend separates HTTP handling (**Controller**), business rules (**Service**), persistence (**Repository**), database entities (**Entity**) and API representations (**DTO**).

- **Repository pattern:** Spring Data JPA repositories encapsulate database access.
- **Adapter pattern:** `OpenLibraryBookAdapter` converts Open Library JSON into the internal `Book` entity.
- **Strategy pattern:** `BookPricingStrategy` defines a pricing contract; `PageBasedPricingStrategy` calculates an illustrative price from the number of pages.
- **Dependency injection:** Spring supplies dependencies through constructors.

## Requirements

- Java **21**
- Node.js and npm (Node.js **22** is used in CI)
- macOS, Linux or a compatible shell for `build.sh`
- An internet connection for dependency installation and Open Library imports

The project uses the Maven Wrapper, so installing Maven separately is not required.

## Build and run (single server)

From the project root:

```bash
./build.sh
```

The script installs frontend dependencies, builds React, copies the compiled files into Spring Boot's static resources, runs the Maven build and tests, and packages the application.

Configure a local JWT signing secret in the terminal before starting the backend:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

**Do not commit the secret to Git.** The secret must be a Base64-encoded value containing at least 32 random bytes. Tokens created with a previous secret become invalid when the secret changes.

Start the packaged application from the `backend` directory:

```bash
cd backend
java -jar target/bookstore-backend-*.jar
```

Open **http://localhost:8080**. The API uses the same host under `/api`.

> The application uses `jdbc:h2:file:./data/bookstoredb`. Run the backend from `backend/` to keep the database location consistent. An initial local database starts without the example data used during development.

### Development mode

Run the backend and frontend in separate terminals:

```bash
# Terminal 1 (from backend/)
export JWT_SECRET="$(openssl rand -base64 32)"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
# Terminal 2 (from frontend/)
npm install
npm run dev
```

In development, Vite normally serves the frontend at **http://localhost:5173** and proxies `/api` requests to **http://localhost:8080**.

The H2 console is enabled only with the `dev` profile at **http://localhost:8080/h2-console**. It is intended for trusted local development only; do not expose the development server or console publicly.

## Authentication and roles

Newly registered accounts have the **USER** role. Registration does not accept administrator privileges.

A successful `POST /api/users/login` response includes an `accessToken`, `tokenType: Bearer`, and `expiresIn: 3600`. For protected API requests, send:

```http
Authorization: Bearer <accessToken>
```

- **Public:** view books and reviews; register and log in.
- **USER:** reserve books; view and cancel own reservations; create reviews; view and delete own reviews.
- **ADMIN:** manage books, import books, inspect book reservations, and mark reservations as collected. Administrators can also use authenticated user functions.

There is **no public endpoint to create an ADMIN account**. In the current educational setup, administrators are assigned through trusted local database administration. This should be replaced with a controlled provisioning process in a deployed system.

## Main REST endpoints

| Method | Endpoint | Access |
| --- | --- | --- |
| `GET` | `/api/books` | Public |
| `GET` | `/api/books/{isbn}` | Public |
| `POST` | `/api/books` | ADMIN |
| `PUT` | `/api/books/{isbn}` | ADMIN |
| `DELETE` | `/api/books/{isbn}` | ADMIN |
| `POST` | `/api/books/import/{isbn}` | ADMIN |
| `POST` | `/api/users/register` | Public |
| `POST` | `/api/users/login` | Public |
| `POST` | `/api/reservations` | Authenticated; matching user ID |
| `GET` | `/api/reservations/user/{userId}` | Owner |
| `GET` | `/api/reservations/book/{isbn}` | ADMIN |
| `DELETE` | `/api/reservations/{id}` | Owner; uncollected only |
| `PATCH` | `/api/reservations/{id}/collect` | ADMIN |
| `GET` | `/api/reviews/book/{isbn}` | Public |
| `POST` | `/api/reviews` | Authenticated; matching user ID |
| `GET` | `/api/reviews/user/{userId}` | Owner |
| `DELETE` | `/api/reviews/{id}` | Owner |

`POST /api/reservations` accepts `userId` and `isbn`. `POST /api/reviews` accepts `userId`, `isbn`, `rating`, and `comment`. The backend compares the submitted user ID with the authenticated JWT subject.

## Tests and CI

Backend tests:

```bash
cd backend
./mvnw test
```

Frontend quality checks:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

The **Bookstore CI** GitHub Actions workflow runs frontend installation, linting and building, then packages and tests the Spring Boot backend. The workflow requires a repository Actions secret named `JWT_SECRET`.

JUnit and Mockito cover service rules and error cases. Manual Postman and browser tests have been performed for registration, JWT authorization, reservations, reviews, administrator operations and Open Library imports. Automated end-to-end and real-database concurrency coverage are not yet comprehensive.

## Known limitations

- H2 is a file-based development database, not a production database configuration. Hibernate schema updating (`ddl-auto=update`) is used for development.
- Imported Open Library records may have incomplete metadata; in particular, author names may fall back to `Unknown author`.
- The imported-book price is **illustrative**, not an actual retail price.
- React currently stores the JWT in component memory. A page refresh logs the user out; token refresh is not implemented.
- Reservation expiration removes uncollected expired reservations rather than retaining an audit history.
- Static frontend files are copied into the backend during the build; rebuilding is required after frontend changes.
- The application is an educational prototype and has not been hardened or audited for production deployment.

## Source and third-party service

Book metadata is retrieved from the [Open Library API](https://openlibrary.org/developers/api). Open Library metadata can vary by edition and may omit some fields.
