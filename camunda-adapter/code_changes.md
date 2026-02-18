# CLI COMMAND: Unified Orchestration Migration

**Context:** We are moving the current Camunda logic into a dedicated adapter and creating a new Orchestration layer.

## Step 1: Migration of Camunda Adapter
1.  **Rename:** Rename the current project folder from `ccr-backend` to `camunda-adapter`.
2.  **Move:** Move the newly named `camunda-adapter` folder into:
    `/Users/deepakvalechha/Learn/Entra-Demo/entra-workspace/`
3.  **Port Update:** Inside `camunda-adapter/src/main/resources/application.properties`, set:
    `server.port=8082`

## Step 2: Create 'orchestrator-backend'
1.  Create a new Spring Boot project in:
    `/Users/deepakvalechha/Learn/Entra-Demo/entra-workspace/orchestrator-backend`
2.  **Configuration:** Set `server.port=8081` in its `application.properties`.
3.  **Dependencies:** Add `spring-cloud-starter-openfeign` and `spring-boot-starter-web` to the `pom.xml`.

## Step 3: FeignClient (Internal Bridge)
In the `orchestrator-backend`, create `CamundaClient.java` to communicate with the adapter on port 8082.

```java
package com.example.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@FeignClient(name = "camunda-adapter", url = "http://localhost:8082/workflow")
public interface CamundaClient {

    @GetMapping("/cases")
    List<Map<String, Object>> getCases(@RequestParam(value = "status", required = false) String status);

    @GetMapping("/case/{id}/macro")
    Map<String, Object> getMacro(@PathVariable("id") String id);

    @GetMapping("/task/{taskId}/micro")
    Map<String, Object> getMicro(@PathVariable("taskId") String taskId);

    @PostMapping("/task/{taskId}/complete")
    String completeTask(@PathVariable("taskId") String taskId, @RequestBody Map<String, Object> vars);
    
    @PostMapping("/case/{businessKey}/sign")
    String signDocuments(@PathVariable("businessKey") String businessKey);
}