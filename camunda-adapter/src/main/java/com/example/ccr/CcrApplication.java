package com.example.ccr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient;
import org.camunda.community.rest.EnableCamundaRestClient;

@SpringBootApplication
@EnableCamundaRestClient
@EnableExternalTaskClient(baseUrl = "http://localhost:8080/engine-rest")
public class CcrApplication {

	public static void main(String[] args) {
		SpringApplication.run(CcrApplication.class, args);
	}

}
