package com.camunda.training.delegate;

import com.camunda.training.service.CreateTweetService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CallActivityDelegate implements JavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RuntimeService runtimeService;

    public CallActivityDelegate(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        //If you have a big variable and you want to share
        ExecutionEntity executionEntity = (ExecutionEntity) delegateExecution.getProcessInstance();
        String processInstanceId = delegateExecution.getProcessInstanceId();
        //runtimeService.

    }
}
