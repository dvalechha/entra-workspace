# Entra ID BFF & Composable Micro-Frontend Architecture

Welcome to the **Entra Workspace** repository! This project demonstrates a secure, scalable architecture using a **Backend-for-Frontend (BFF)** pattern with Microsoft Entra ID (formerly Azure AD) and **Module Federation** for composable micro-frontends (MFEs).

## 🏗️ Architecture Overview

The system is composed of five distinct applications working together:

### Backends (Java Spring Boot)
1.  **`bff-backend` (Port 3001)**
    -   **Role:** The entry point for the frontend. Handles all authentication (OAuth2/OIDC), session management, and proxies API requests to downstream services.
    -   **Security:** Keeps tokens (Access/Refresh) server-side in an HTTP-only session. The frontend never sees the tokens.
    -   **Key Features:** Manual session validation, Entra ID integration, Proxy logic.

2.  **`data-backend` (Port 3002)**
    -   **Role:** The protected resource server. Provides business data (Metrics, Analytics).
    -   **Security:** Validates JWT Access Tokens passed by the BFF. Enforces Role-Based Access Control (RBAC).
    -   **Key Features:** `@PreAuthorize` annotations for fine-grained security.

### Frontends (React + Vite + Module Federation)
3.  **`react-bff-app` (Shell - Port 5173)**
    -   **Role:** The main container application. Handles login/logout, navigation, and dynamically loads remote MFEs.
    -   **Key Features:** Module Federation Host, Auth state management.

4.  **`react-metrics` (MFE - Port 5178)**
    -   **Role:** A micro-frontend exposing a "Metrics" dashboard.
    -   **Access:** Restricted to users with `role.alpha`.

5.  **`react-analytics` (MFE - Port 5179)**
    -   **Role:** A micro-frontend exposing an "Analytics" dashboard.
    -   **Access:** Restricted to users with `role.beta`.

---

## 🔐 Roles & Features

Access to features is strictly controlled by Entra ID App Roles assigned to your user account.

| Entra ID Role | Feature Access | API Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **`role.alpha`** | **Metrics Dashboard** | `/v1/data/metrics` | View system health and performance metrics. |
| **`role.beta`** | **Analytics Dashboard** | `/v1/data/analytics` | View user growth and engagement analytics. |

> **Note:** The Shell App (`react-bff-app`) dynamically builds the sidebar menu based on the roles present in your ID Token. If you don't have a role, you won't see the corresponding menu item!

---

## 🚀 Getting Started

### Prerequisites
-   **Java 17+** (for Spring Boot backends)
-   **Node.js 18+** (for React frontends)
-   **Maven** (Wrapper `mvnw` is included)

### 1. Start the Backends
Open two terminal windows/tabs:

**Terminal 1 (BFF):**
```bash
cd bff-backend
./mvnw spring-boot:run
```
*Wait for it to start on port 3001.*

**Terminal 2 (Data Backend):**
```bash
cd data-backend
./mvnw spring-boot:run
```
*Wait for it to start on port 3002.*

### 2. Start the Frontends
Open three terminal windows/tabs:

**Terminal 3 (Metrics MFE):**
```bash
cd react-metrics
npm install
npm run dev
```
*Runs on port 5178.*

**Terminal 4 (Analytics MFE):**
```bash
cd react-analytics
npm install
npm run dev
```
*Runs on port 5179.*

**Terminal 5 (Shell App):**
```bash
cd react-bff-app
npm install
npm run dev
```
*Runs on port 5173.*

### 3. Access the Application
Open your browser and navigate to:
👉 **http://localhost:5173**

---

## ⚠️ Important: Testing & Login Access

The application uses **Microsoft Entra ID** for authentication.

**To actually log in and test the application, your Microsoft account (email) MUST be pre-registered in the Entra ID tenant.**

### If you want to test the full login flow:
1.  **Contact the Repository Owner (Deepak)**.
2.  Provide **two Microsoft account emails** you wish to use for testing.
3.  The owner will:
    -   Add these users to the Entra ID App Registration.
    -   Assign **`role.alpha`** to one user (to test Metrics access).
    -   Assign **`role.beta`** to the other user (to test Analytics access).

### If you cannot contact the owner:
You can still explore the codebase to understand the architectural patterns:
-   **BFF Pattern:** See `bff-backend/src/main/java/com/example/entra/bff_backend/controller/ProxyController.java`.
-   **RBAC:** See `data-backend/src/main/java/com/example/entra/data_backend/controller/DataController.java`.
-   **Module Federation:** See `react-bff-app/vite.config.ts`.

However, without being added to the tenant, clicking "Login" will result in an Entra ID error.

---

## 🛠️ Key Commands Summary

| Service | Command | Port |
| :--- | :--- | :--- |
| BFF Backend | `./mvnw spring-boot:run` | 3001 |
| Data Backend | `./mvnw spring-boot:run` | 3002 |
| Shell App | `npm run dev` | 5173 |
| Metrics MFE | `npm run dev` | 5178 |
| Analytics MFE | `npm run dev` | 5179 |

---

## 🛠️ Installation Guide

### 🍎 macOS
#### 1. Install Java 17+
The easiest way is using [Homebrew](https://brew.sh/):
```bash
brew install openjdk@17
```
Follow the post-installation instructions to add it to your PATH.

#### 2. Install Maven
```bash
brew install maven
```

### 🪟 Windows
#### 1. Install Java 17+
Using **winget** (built-in package manager):
```powershell
winget install Microsoft.OpenJDK.17
```
Alternatively, download the installer from [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download).

#### 2. Install Maven
Using **winget**:
```powershell
winget install Apache.Maven
```
Alternatively, download from the [Apache Maven website](https://maven.apache.org/download.cgi) and follow the manual installation steps.
