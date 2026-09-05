# Matchly

Matchly is a full-stack real-time matchmaking and chat application that connects users with random strangers for one-on-one conversations.

The project explores real-time communication, authentication, concurrent matchmaking, and secure room-based messaging using a React frontend and a Java Spring Boot backend.

> Matchly is currently under active development.

## Features

- User registration and authentication
- JWT-based API authentication
- Redis-powered stranger matchmaking
- Real-time private messaging with WebSockets and STOMP
- Match-specific chat rooms
- Partner disconnect detection
- "Next Stranger" rematching
- PostgreSQL user persistence
- Email verification foundation
- Responsive React interface

## Tech Stack

### Frontend

- React
- JavaScript
- Vite
- React Router
- STOMP.js
- CSS

### Backend

- Java
- Spring Boot
- Spring Security
- Spring WebSocket
- Spring Data JPA
- Spring Data Redis

### Data & Infrastructure

- PostgreSQL
- Redis
- Docker

## How Matchmaking Works

When a user requests a match, the backend uses Redis to place the user into the matchmaking system.

If another user is waiting, the two users are paired and assigned to the same match.

After matching, both clients connect to a private WebSocket destination associated with their room and can exchange real-time messages.

Matchmaking operations are designed to be atomic so multiple users joining at the same time cannot accidentally be paired more than once.

## Real-Time Chat

Matchly uses WebSockets with STOMP for real-time communication.

Authenticated users connect using their JWT access token. The backend determines the user's identity from the authenticated WebSocket session rather than trusting identity information sent by the client.

Users are only allowed to subscribe to and send messages inside rooms belonging to their active match.

## Project Structure

```text
Matchly/
├── frontend/
│   └── React application
│
├── backend/
│   └── Spring Boot application
│
├── docker/
│   └── Local PostgreSQL and Redis configuration
│
└── docs/
    └── Project documentation
```

## Running Locally

### Requirements

You will need:

- Node.js
- Java
- Docker
- Git

### Start PostgreSQL and Redis

Start Docker Desktop and launch the project's containers.

### Start the backend

```bash
cd backend

set -a
source ../docker/.env
set +a

./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Start the frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

## Security

Matchly currently includes several security measures:

- Password hashing
- JWT authentication
- Authenticated WebSocket connections
- Room-level WebSocket authorization
- Server-controlled sender identity
- Hashed email verification tokens
- Expiring verification tokens

Secrets and credentials are stored using environment variables and are excluded from Git.

## Current Development

The core real-time text matchmaking system is functional.

Current development is focused on completing account verification, improving the user experience, testing the application, and preparing Matchly for deployment.

Future improvements may include peer-to-peer video communication, moderation tools, rate limiting, and production deployment.

## What I Learned

Building Matchly has given me hands-on experience with:

- Designing a full-stack application
- Connecting React to a REST API
- Building authentication with JWT
- Working with PostgreSQL through Spring Data JPA
- Using Redis for concurrent matchmaking
- Implementing real-time communication with WebSockets
- Securing WebSocket connections and chat rooms
- Working with Dockerized development services
- Using Git and GitHub for version control

## Status

🚧 **In development**

The project is being actively improved and prepared for deployment.


# PSA: Security flaws and bugs that I'm stil working on aug/31/26

- **Fake email addresses are accepted:** Users can sign up or log in using invalid or fake email addresses. Email verification or proper authentication should be implemented.
- **Chat ending is not synchronized between users:** When one person ends the chat, the other person is not notified and can continue sending messages. The chat session should immediately end for both users.
- **Inappropriate image detection:** The platform should include image moderation. If a user uploads inappropriate or prohibited content, the system should automatically detect it and take action, such as removing the image, warning the user, suspending the account, or banning the user depending on the severity.
- **Incorrect “Matched” status:** After logging in, the system sometimes displays that the user has already been matched even though they have not started searching for a chat partner. This issue was reproduced using two different browsers.
- **Users can be matched with the same person again:** After ending a conversation and searching for a new match, users may be matched with someone they have already spoken to. The matchmaking system should reduce or prevent immediate repeat matches.
- **Privacy, Safety, and Terms links are not functional:** The **Privacy**, **Safety**, and **Terms** sections appear to be incomplete or not properly connected to their respective pages.
- **Guest mode description may be misleading:** The statement, *“No signup required for guest mode. Fast, simple, and safety-focused,”* should be reviewed to make sure guest mode actually works as described and has appropriate safety and privacy protections.
