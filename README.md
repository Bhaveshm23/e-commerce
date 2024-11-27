# Curbside Pickup Service

## Overview

The **Curbside Pickup Service** is a microservices-based application designed to streamline the order placement, inventory tracking, and order fulfillment processes for curbside pickups. The modular architecture has separate services for different functionalities, ensuring scalability and maintainability.

---

## Architecture

### Key Microservices

1. **Order Service**  
   - **Purpose**: Handles order placement and manages order information flow to other services.  
   - **Database**: MySQL.

2. **Product Service**  
   - **Purpose**: Maintains product details, including descriptions, pricing, and availability.  
   - **Database**: MongoDB.  

3. **Inventory Service**  
   - **Purpose**: Tracks inventory levels and updates stock based on orders.  
   - **Database**: MySQL.

4. **Order-Picking Service**  
   - **Purpose**: Manages the order-picking process, including assigning pickers, tracking statuses (e.g., UNASSIGNED, PICKING, STAGED, PARTIALLY STAGED), and ensuring efficient fulfillment.  
   - **Database**: MySQL.  

5. **User-Picking Service (To Be Developed)**  
   - **Purpose**: Allows users to pick up their orders after fulfillment by the order-picking service.  
   - **Database**: MySQL (proposed).

---

## Workflow

1. **Order Placement**  
   - Users place orders through the **Order Service**.  
   - Orders are passed to the **Order-Picking Service** for fulfillment.  

2. **Product Management**  
   - The **Product Service** keeps track of product metadata.  

3. **Inventory Management**  
   - The **Inventory Service** ensures stock levels are up-to-date and adjusted based on orders.  

4. **Order Fulfillment**  
   - The **Order-Picking Service** manages the stages of order fulfillment and ensures operational efficiency.  

5. **User Pickup**  
   - Once developed, the **User-Picking Service** will enable users to pick up their fulfilled orders.

---

## Technology Stack

### Backend
- **Languages**: Java  
- **Frameworks**: Spring Boot 

### Databases
- **MySQL**: Used by Order Service, Inventory Service, and Order-Picking Service.  
- **MongoDB**: Used by Product Service.

### Build and Deployment
- **Tools**: Gradle/Maven for build automation.  

---

## Future Plans

- Integrate real-time notifications for order status updates.  
- Optimize database queries for faster performance.  

---

## How to Run

1. Clone this repository.  
2. Navigate to the respective service directories (e.g., `order-service`, `product-service`).  
3. Build the services using Gradle or Maven.  
4. Deploy each service using Docker or any preferred deployment tool.  
5. Configure MySQL and MongoDB databases for respective services.

