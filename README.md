# Adapter WebSocket Service

This is the initial version of the **Adapter WebSocket Service**, designed to provide real-time cryptocurrency rate updates to clients via WebSocket connections. This service acts as a bridge, consuming rate events from a message broker and broadcasting them to subscribed users.

**Note:** This is an initial version of the project and is open to future updates and enhancements.

---

## 🚀 Key Features

* **Real-time Streaming**: Provides live crypto rate updates using Spring WebSocket and STOMP.
* **Dynamic Subscriptions**: Clients can subscribe to and unsubscribe from specific currency pairs (e.g., BTC-USD) on the fly.
* **Efficiency**: Automatically enables or disables upstream rate streaming based on active subscriber counts to save resources.
* **Fault Tolerance**: Includes a retry mechanism using PostgreSQL and ShedLock for handling failed disable requests to upstream services.
* **Session Management**: Monitors active connections and enforces token expiration checks to ensure security.

---

## 🛠 Tech Stack

* **Framework**: Spring Boot 3.3.3
* **Language**: Java 21
* **Messaging**: RabbitMQ (Spring AMQP)
* **Caching/Session Storage**: Redis
* **Database**: PostgreSQL 
* **Migration**: Liquibase
* **Communication**: OpenFeign (for internal service calls)
