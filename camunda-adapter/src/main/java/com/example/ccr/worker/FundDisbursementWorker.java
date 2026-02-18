package com.example.ccr.worker;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@ExternalTaskSubscription(topicName = "fund-loan") // This matches the 'Topic' in the BPMN file
public class FundDisbursementWorker implements ExternalTaskHandler {

    private static final Logger LOG = Logger.getLogger(FundDisbursementWorker.class.getName());

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        // 1. Get data from the workflow
        String businessKey = externalTask.getBusinessKey();
        LOG.info("💰 STARTING DISBURSEMENT for Case: " + businessKey);

        try {
            // 2. Simulate complex banking logic (API calls to Core Banking)
            LOG.info("... Connecting to SWIFT network ...");
            Thread.sleep(2000); // Fake processing time
            LOG.info("... Transfer Complete.");

            // 3. Complete the task
            externalTaskService.complete(externalTask);
            LOG.info("✅ Case Funded Successfully!");

        } catch (InterruptedException e) {
            // Handle errors (create an incident in Camunda)
            externalTaskService.handleFailure(externalTask, "Funding Failed", e.getMessage(), 0, 1000);
        }
    }
}