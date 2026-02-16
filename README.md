# 🚀 Entra-Demo Workspace: Master Overview

This workspace implements a high-performance **Micro-Frontend (MFE)** architecture coupled with a **Headless Workflow Engine** and a centralized **Orchestration Layer**. The system is designed for enterprise-grade scalability, secure identity management via **Microsoft Entra ID**, and a strictly decoupled backend strategy.

---

## 🏗️ System Architecture

The ecosystem is structured into three distinct logical layers to ensure separation of concerns and ease of maintenance.

### 1. Presentation Layer (Micro-Frontends)
* **`react-bff-app`**: The "Shell" or Host application. It manages the global navigation, shared user session state, and the core **Credit Intake Dashboard**.
* **`react-analytics`**: A specialized MFE dedicated to deep data visualization and historical trend analysis.
* **`react-metrics`**: A specialized MFE providing real-time operational performance metrics and SLA tracking.

### 2. Orchestration & Security Layer
* **`orchestrator-backend` (Port 8081)**: The primary API gateway for the UI. It uses **FeignClients** to aggregate data from multiple downstream adapters into a unified response.
* **`bff-backend`**: Dedicated to security and identity. It manages **Microsoft Entra ID** authentication, token validation, and OIDC handshakes.

### 3. Service & Adapter Layer
* **`camunda-adapter` (Port 8082)**: A native bridge to the **Camunda 7** engine. It provides a business-centric API for process tracking, including "Macro" (roadmap) and "Micro" (task-level) views.


---

## 🚦 Network & Connectivity Map

To maintain a secure internal perimeter, all traffic follows a tiered communication path:

1.  **Frontend ➔ Node Proxy**: The React dev server proxies `/workflow` calls to avoid CORS issues and hide internal ports.
2.  **Proxy ➔ Orchestrator (8081)**: The single entry point for all business requests.
3.  **Orchestrator ➔ Adapter (8082)**: High-efficiency internal communication via **Spring Cloud OpenFeign**.
4.  **Adapter ➔ Engine (8080)**: Native Java API calls to the Camunda process engine.

---

## 🛠️ Technology Stack

| Component | Technology | Primary Role |
| :--- | :--- | :--- |
| **Frontend UI** | React 18, Tailwind CSS | Modular UI & MFE Shell |
| **Orchestrator** | Spring Boot 3, Feign | Data Aggregation & Routing |
| **Workflow** | Camunda 7 (Embedded) | State Machine & BPMN Logic |
| **Identity** | MS