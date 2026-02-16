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