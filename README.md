# Ticket Management System

## Overview
Welcome to the Distributed Systems Course Project: Ticket Management System repository! This project, implemented in Java using the Spring Boot framework, utilizes TCP, UDP, multicast protocols, and a Spring Boot REST API to handle load, maintain data synchronization across multiple servers, and effectively manage tickets. It was developed as part of a distributed programming course and received a final score of 86 out of 100.

## Project Highlights
### Distributed System Architecture
The system comprises three main components:

1. **Clients:** Initiate requests to the system to create, view, update, or delete tickets.

2. **Servers:** Receive requests from clients, process them, and store data locally. To maintain data consistency, each server maintains a **local database** that reflects the current state of the ticket information.

3. **Global Registry:** Acts as a central coordinator, keeping track of all active servers and providing clients with the addresses of appropriate servers for connection.

4. **Spring Boot REST API:** Serves as an interface between clients and servers, providing endpoints for various ticket management operations.

### Local Databases
Each server maintains a **local database** to store ticket data. This local storage ensures that servers can operate efficiently without relying on constant communication with other servers.

### Data Synchronization
To maintain data consistency across servers, a **synchronization mechanism** is employed. Whenever a change occurs in a server's local database, it broadcasts the updated data to other servers. Receiving servers integrate the updated information into their own local databases, ensuring that all servers have the most up-to-date ticket information.

### Load Balancing and Scalability
A **load balancing mechanism** distributes requests evenly among servers to prevent any single server from becoming overloaded. This is achieved using a **round-robin scheduling technique** for assigning requests to servers. The system is also designed to be **scalable**, allowing for the addition of more servers to handle increasing ticket volume and user load. With more servers, the load is distributed more efficiently, improving system performance and stability.

### Spring Boot REST API
A **Spring Boot REST API** is incorporated to provide a structured and efficient interface for ticket management. The API utilizes **Spring Boot's dependency injection framework** for dependency management and **Spring MVC** for routing requests and handling responses. API endpoints follow **RESTful principles**, making it easier for users to interact with the system's ticket data.

