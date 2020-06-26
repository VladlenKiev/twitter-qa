package com.camunda.training.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("mapping")
public class VariableMappingExampleDelegate implements DelegateVariableMapping {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    @Override
    public void mapInputVariables(DelegateExecution delegateExecution, VariableMap variableMap) {

    }

    @Override
    public void mapOutputVariables(DelegateExecution delegateExecution, VariableScope variableScope) {

    }
}
