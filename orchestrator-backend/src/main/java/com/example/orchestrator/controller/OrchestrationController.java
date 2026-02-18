package com.example.orchestrator.controller;

import com.example.orchestrator.client.CamundaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class OrchestrationController {

    @Autowired
    private CamundaClient camundaClient;

    // React calls this via: fetch('/workflow/cases?status=running')
    @GetMapping("/cases")
    public List<Map<String, Object>> getDashboard(@RequestParam(required = false) String status) {
        return camundaClient.getCases(status);
    }

    // React calls this to get the Roadmap/Chevron data
    @GetMapping("/case/{id}/macro")
    public Map<String, Object> getRoadmap(@PathVariable String id) {
        return camundaClient.getMacro(id);
    }

    // React calls this when a user submits the checklist/form
    @PostMapping("/task/{id}/complete")
    public String completeUserWork(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        return camundaClient.completeTask(id, payload);
    }
}
