package com.camunda.training;

import com.camunda.training.delegate.PublishTweetDelegate;
import com.camunda.training.service.CreateTweetService;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.entry;

public class ProcessJUnitTest {

    private static final String PROCESS_DEFINITION_KEY = "ProcessApproveTweetKey";
    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();
//  public ProcessEngineRule rule = new ProcessEngineRule();

    @InjectMocks
    private PublishTweetDelegate publishTweetDelegate;
    @Mock
    private CreateTweetService createTweetService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mocks.register("publishTweetDelegate", publishTweetDelegate);
        init(rule.getProcessEngine());
    }

    //  @Test
//  @Deployment(resources = "twitter-project-mankovskyi.bpmn")
    public void testHappyPath() {
        // Create a HashMap to put in variables for the process instance
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 4 test - MANKOVSKYI");
        variables.put("approved", true);
        // Start process with Java API and variables
        //ProcessApproveTweet
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
        assertThat(processInstance).isWaitingAt("ReviewTweetTask");
        complete(task(), withVariables("approved", false));
        assertThat(processInstance).hasNotPassed("PublishTweetTask");
        assertThat(processInstance).hasPassed("DeclineTweetTask");

        // Make assertions on the process instance
        assertThat(processInstance).isEnded();

    }

    @Test
    @Deployment(resources = {"tweetApproval.dmn", "twitter-project-mankovskyi.bpmn"})
    public void testHappyPath_withTwitterIntegration() {
        // Start process with Java API and variables
        //ProcessApproveTweet
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("email", "jakob.freund@camunda.com");
        variables.put("content", "this should be published");
//        variables.put("approved", true);
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
        assertThat(processInstance).hasPassed("ReviewTweetTask");

        assertThat(processInstance).hasPassed("IsApproveGateway");
        assertThat(processInstance).hasPassed("PublishTweetTask");
        assertThat(processInstance).hasNotPassed("DeclineTweetTask");

        // Make assertions on the process instance
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = {"tweetApproval.dmn", "twitter-project-mankovskyi.bpmn"})
    public void testTweetRejected_withTwitterIntegration() {

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 4 test - MANKOVSKYI");
        variables.put("approved", false);
//        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

        ProcessInstance processInstance = runtimeService()
                .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
                .setVariables(variables)
                .startAfterActivity("ReviewTweetTask")
                .execute();

        assertThat(processInstance).hasPassed("IsApproveGateway");
        assertThat(processInstance).hasNotPassed("PublishTweetTask");
        assertThat(processInstance).hasPassed("DeclineTweetTask");

        assertThat(processInstance).isWaitingAt("NotifyUserOfRejectionTask")
                .externalTask()
                .hasTopicName("notificationTweet");
        complete(externalTask());

        assertThat(processInstance).hasPassed("NotifyUserOfRejectionTask");


        // Make assertions on the process instance
        assertThat(processInstance).isEnded();

    }

    @Test
    @Deployment(resources = {"tweetApproval.dmn", "twitter-project-mankovskyi.bpmn"})
    public void happyPath_withMessaging() {

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 4 test - MANKOVSKYI");
        variables.put("approved", true);

        ProcessInstance processInstance = runtimeService()
                .createMessageCorrelation("superUserTweet")
                .setVariable("content", "My Exercise 11 Tweet (Mankovskyi) - " + System.currentTimeMillis())
                .correlateWithResult()
                .getProcessInstance();

        assertThat(processInstance).isStarted();

/*        runtimeService()
                .createMessageCorrelation("tweetWithdrawn")
                .processInstanceId(processInstance.getId())
                .correlateWithResult();
        */

        // get the job
        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        // execute the job
        Assertions.assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);

        // Make assertions on the process instance
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = "tweetApproval.dmn")
    public void testTweetFromJakob() {
        Map<String, Object> variables = withVariables("email", "jakob.freund@camunda.com", "content", "this should be published");
        DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval", variables);

        Assertions.assertThat(decisionResult.getFirstResult()).contains(entry("approved", true));
    }

}
