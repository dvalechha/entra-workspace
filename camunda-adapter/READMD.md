# Project Context: CCR Backend (Headless Camunda)

## 1. Core Architecture
* **Tech Stack:** Spring Boot 3.x, Java 17, Camunda 7 (External Task Client).
* **Pattern:** Headless Workflow Engine.
    * **Orchestrator:** Camunda 7 running in Docker (Port 8080).
    * **Backend API:** Spring Boot (Port 8081) acting as the bridge.
    * **Frontend:** React App (Planned) consuming Spring Boot endpoints.
* **Ingestion:** Kafka-driven intake (Planned) triggering process starts.

## 2. The Workflow (Commercial Credit Intake)
* **Process Definition Key:** `process_ccr_intake`
* **BPMN Stages:**
    1.  **Start:** New Credit Request.
    2.  **User Task:** Queue Manager Review (Initial vetting).
    3.  **Gateway:** Approved? (Yes/No).
    4.  **User Task:** Draft Loan Agreement.
    5.  **Service Task:** Generate Legal Package (Automated).
    6.  **Catch Event:** Wait for Signature (External Message).
    7.  **User Task:** Verify Signed Docs (Final Human Check).
    8.  **External Task:** Disburse Funds (Automated Worker - Topic: `fund-loan`).

## 3. Custom API Endpoints (`WorkflowController.java`)
The backend provides a "Business-Speak" wrapper over Camunda's "Technical-Speak":

| Endpoint | Purpose | Camunda Service Used |
| :--- | :--- | :--- |
| `GET /cases` | Dashboard: List all running/finished cases. | `HistoryService` |
| `GET /case/{id}/macro` | Roadmap: Progress %, Phase Name, and `activeTaskId`. | `History` + `Runtime` |
| `GET /task/{id}/micro` | Work Desk: Returns `formKey` and process variables. | `TaskService` |
| `POST /task/{id}/complete` | Action: Completes a User Task from React UI. | `TaskService` |
| `POST /case/{key}/sign` | Signal: Correlates signature message to instance. | `RuntimeService` |

## 4. Current Working Logic (Caveats)
* **Macro Mapping:** The `currentPhase` and `progressPercent` are calculated in the Controller by mapping `taskDefinitionKey` or `taskName` to business labels.
* **Identity:** The system supports lookup via **Internal Process Instance ID** or the **Business Key** (e.g., `998`).
* **Manual Steps:** Multi-step manual work is handled via a **Checklist Pattern** inside single User Tasks, driven by the `formKey`.

## 5. Active Debugging Status
* Ensure Camunda Docker is running before starting Spring Boot.
* The External Task Worker for `fund-loan` is embedded within the Spring Boot app.