package com.example.ccr.controller;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    /**
     * DASHBOARD: Fetch all cases with status filtering.
     * GET /workflow/cases?status=running|completed
     */
    @GetMapping("/cases")
    public List<Map<String, Object>> getAllCases(@RequestParam(required = false) String status) {
        var query = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc();

        if ("running".equalsIgnoreCase(status)) query.unfinished();
        else if ("completed".equalsIgnoreCase(status)) query.finished();

        return query.list().stream().map(hi -> {
            Map<String, Object> map = new HashMap<>();
            map.put("caseId", hi.getId());
            map.put("businessKey", hi.getBusinessKey());
            map.put("status", hi.getEndTime() == null ? "RUNNING" : "COMPLETED");
            map.put("startTime", hi.getStartTime());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * MACRO VIEW: High-level roadmap and link to active task.
     * GET /workflow/case/{lookupId}/macro
     */
    @GetMapping("/case/{lookupId}/macro")
    public Map<String, Object> getMacroView(@PathVariable String lookupId) {
        // Lookup by Business Key (998) or Internal ID
        HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(lookupId).singleResult();
        if (hi == null) hi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(lookupId).singleResult();

        if (hi == null) throw new RuntimeException("Case not found");

        boolean isFinished = hi.getEndTime() != null;
        Map<String, Object> res = new HashMap<>();
        res.put("caseId", hi.getId());
        res.put("businessKey", hi.getBusinessKey());
        res.put("isLive", !isFinished);

        if (!isFinished) {
            Task activeTask = taskService.createTaskQuery().processInstanceId(hi.getId()).singleResult();
            if (activeTask != null) {
                res.put("activeTaskId", activeTask.getId());
                res.put("activeTaskName", activeTask.getName());
                // Business Phase Mapping
                if (activeTask.getName() != null && activeTask.getName().contains("Review")) res.put("currentPhase", "Intake Review");
                else res.put("currentPhase", activeTask.getName());
            }
        } else {
            res.put("currentPhase", "Completed");
        }
        return res;
    }

    /**
     * MICRO VIEW: Task-specific details and Form Key.
     * GET /workflow/task/{taskId}/micro
     */
    @GetMapping("/task/{taskId}/micro")
    public Map<String, Object> getMicroView(@PathVariable String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new RuntimeException("Task not found");

        Map<String, Object> res = new HashMap<>();
        res.put("taskId", task.getId());
        res.put("formKey", task.getFormKey());
        res.put("variables", taskService.getVariables(taskId));
        return res;
    }

    /**
     * ACTION: Complete a user task.
     * POST /workflow/task/{taskId}/complete
     */
    @PostMapping("/task/{taskId}/complete")
    public String completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> vars) {
        taskService.complete(taskId, vars);
        return "✅ Task " + taskId + " completed.";
    }

    /**
     * SIGNAL: Correlate signature message to move past wait state.
     * POST /workflow/case/{businessKey}/sign
     */
    @PostMapping("/case/{businessKey}/sign")
    public String signDocuments(@PathVariable String businessKey) {
        runtimeService.createMessageCorrelation("Message_SignatureReceived")
                .processInstanceBusinessKey(businessKey)
                .correlate();
        return "✅ Signature correlated for " + businessKey;
    }
}
