# FlatFlow

**A shared household management application for roommates.**

FlatFlow is a web-based application designed to make living with roommates easier by bringing household organization into one place. It aims to help housemates coordinate chores, track shared expenses, and manage their household together.

Developed as a team project for **CS2043 — Software Development** at the University of New Brunswick.

---

## Features

### User Authentication
- Create an account with a display name, login name, and password.
- Log in securely using token-based authentication.
- Log out of an active session.

### Household Management
- Create a household.
- Generate a unique household invitation code.
- Join an existing household using an invitation code.
- View household members and their roles.
- Distinguish between household organizers and members.

### Chore Management — Frontend Prototype
- Create chores and assign them to household members.
- Set due dates and track completion status.
- Filter chores by assignee or status.
- Identify overdue chores.

### Shared Expenses — Frontend Prototype
- Record shared household expenses.
- Select the member who paid.
- Split expenses equally among selected members.
- Calculate individual expense shares.

### Household Dashboard — Frontend Prototype
- View pending and overdue chores.
- See household member balances.
- Get an overview of household activity.

> **Development status:** Authentication and household management are connected to the Java backend. Chores, expenses, and dashboard pages have frontend implementations and mock API support, but their real backend endpoints are not yet implemented.

---

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java, Javalin |
| Database | SQLite |
| Build & Dependencies | Apache Maven |
| Testing | JUnit |
| Version Control | Git & GitHub |

---

## Project Structure

```text
FlatFlow/
├── backend/
│   ├── config.json
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/flatflow/
│       │   │   ├── config/
│       │   │   ├── data/
│       │   │   ├── model/
│       │   │   ├── service/
│       │   │   ├── web/
│       │   │   └── App.java
│       │   └── resources/
│       └── test/
├── frontend/
│   ├── js/
│   ├── index.html
│   ├── register.html
│   ├── household.html
│   ├── dashboard.html
│   ├── chores.html
│   ├── expenses.html
│   └── style.css
└── README.md
```

---

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java Development Kit (JDK)
- Apache Maven
- Git
- A modern web browser

### 1. Clone the Repository

```bash
git clone https://github.com/mahinmaj-08/FlatFlow.git
cd FlatFlow
```

### 2. Run the Backend

```bash
cd backend
mvn compile exec:java -Dexec.mainClass=flatflow.App
```

### 3. Open FlatFlow

Once the server is running, open:

**http://localhost:8080/**

The Java backend serves the frontend and handles supported API requests.

### 4. Try the Application

1. Register a new account.
2. Log in with your credentials.
3. Create a household.
4. Copy the generated invitation code.
5. Register another account and join the household using that code.
6. View the household members and their roles.

---

## Running Tests

From the `backend` directory, run:

```bash
mvn clean test
```

The current automated test suite contains four tests covering authentication and household management.

**Latest verified result:** 4 tests passed, 0 failures, 0 errors.

---

## Git Workflow

We use a branching workflow to keep development organized.

| Branch | Purpose |
|---|---|
| `main` | Integrated, tested project version |
| `develop` | Integration branch for ongoing development |
| `feature/frontend-pages` | Frontend development |
| `feature/backend` | Backend development |

Feature branches are merged into `develop`, tested, and then integrated into `main`.

---

## Development Roadmap

- [x] Initialize GitHub repository
- [x] Establish frontend and backend project structure
- [x] Implement user registration and login
- [x] Implement household creation and joining
- [x] Display household members and roles
- [x] Connect authentication and household frontend to backend
- [x] Add automated backend tests
- [x] Merge and publish Increment 1
- [ ] Implement real chore management API
- [ ] Implement real expense management API
- [ ] Connect dashboard to backend data
- [ ] Expand automated and integration testing
- [ ] Improve user experience and error handling

---

## Contributors

| Team Member | GitHub | Contributions |
|---|---|---|
| Mahin Majid | [@mahinmaj-08](https://github.com/mahinmaj-08) | Frontend development, UI pages, JavaScript API client, frontend/backend integration, Git branch integration |
| Saumya J | [@Sj011106](https://github.com/Sj011106) | Java backend, authentication, household services, SQLite persistence, API controllers, unit testing |

---

## Academic Project

FlatFlow is developed for **CS2043 — Software Development** at the **University of New Brunswick**.

**Current milestone:** Deliverable 3 — Implementation, Increment 1.

This project is under active development. Additional functionality will be implemented and integrated in subsequent increments.

---

**FlatFlow — Making shared living a little simpler. **
