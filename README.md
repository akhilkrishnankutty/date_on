# DateOn Backend

The DateOn Backend is a robust Spring Boot application that orchestrates user management, real-time matchmaking, secure authentication, and integrates with a specialized AI matchmaking engine. 

## 🚀 Features

* **JWT Authentication:** Secure user signup and login flow utilizing Spring Security and JWT.
* **AI Matchmaking Engine:** Communicates with an external Python-based AI service to analyze user "vibe check" quiz answers and calculate compatibility scores.
* **Fallback Matcher:** Purely relational database-fallback algorithms to gracefully match users mathematically if the AI pipeline is offline.
* **Kafka Event-Driven Architecture:** Uses Apache Kafka to queue, process, and handle asynchronous user matching (`Matcher.java`) and messaging events securely.
* **Unmatch & Cooldown Integrity:** Incorporates a strict 5-day timeout period for users who initiate unmatching, automatically managing database user `COOLDOWN` states.
* **Profile Management:** Secure Base64/Multipart handling of profile pictures.

## 🛠️ Technology Stack

* **Java 17+**
* **Spring Boot (Web, JPA, Security)**
* **MySQL Base Storage**
* **Apache Kafka (Message Queuing)**
* **JSON Web Tokens (io.jsonwebtoken)**

## 📦 Local Setup & Installation

1. Ensure **MySQL** is running and creating the necessary database/schema according to your `application.properties`.
2. Ensure **Apache Kafka** is running locally (or pointing to a remote cluster via `application.properties`).
3. Run `mvn clean install` to resolve dependencies.
4. Run the Spring Boot Application via `DateonApplication.java` or `mvn spring-boot:run`.

## 📜 Key Endpoints
* `POST /user/login` - Authenticate and retrieve Bearer token 
* `POST /user/create` - Create a new user profile
* `POST /user/{id}/profile-picture` - Upload/Update a user profile picture
* `POST /user/{id}/unmatch` - Unmatch current partner and incur a 5-day matchmaking cooldown
* `POST /user/{id}/unlock-cooldown` - Manually unlock the 5-day cooldown penalty 

*Note: All endpoints besides `/create` and `/login` require a valid JWT Bearer Token in the `Authorization` header.*
