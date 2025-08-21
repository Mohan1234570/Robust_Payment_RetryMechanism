<p align="center">

# High-level payment system architecture diagram
</p>




<img width="2379" height="1580" alt="image" src="https://github.com/user-attachments/assets/6f1320c9-f9a9-4e98-991f-14b92041dade" />


# Robust Payment Retry Mechanism App

A **Spring Boot** application that integrates with **Stripe** to handle secure payment processing with a **robust retry mechanism**.  
It supports **webhook handling**, **database persistence** using PostgreSQL, and **idempotent retries** to ensure payment consistency in case of failures.  

---

## 🚀 Features
- Stripe Checkout integration  
- Secure webhook listener (`/api/payments/webhook`)  
- Automatic retry mechanism for failed payments  
- PostgreSQL persistence for payment records  
- Configurable retry logic with exponential backoff  
- Spring Boot + JPA + Hibernate stack  

---

## 🛠️ Tech Stack
- **Backend:** Java 17, Spring Boot  
- **Database:** PostgreSQL  
- **Payments:** Stripe API + Webhooks  
- **Build Tool:** Maven  
- **Deployment:** Docker / Cloud-ready  

---

## ⚙️ Prerequisites
Make sure you have installed:
- Java 17+  
- Maven 3+  
- PostgreSQL (running locally or in Docker)  
- A [Stripe account](https://stripe.com/)  

---

## 📂 Project Structure
<img width="535" height="783" alt="image" src="https://github.com/user-attachments/assets/2748ccb1-9b38-4b65-aa7d-aeba5e27826c" />

