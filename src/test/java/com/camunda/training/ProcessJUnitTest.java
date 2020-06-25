package com.camunda.training;

import com.camunda.training.delegate.PublishTweetDelegate;
import com.camunda.training.service.CreateTweetService;
import org.assertj.core.api.Assertions;
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
    @Deployment(resources = "twitter-project-mankovskyi.bpmn")
    public void testHappyPath_withTwitterIntegration() {
        // Create a HashMap to put in variables for the process instance
   /* HashMap<String, Object> variables = new HashMap<>();
    variables.put("approved",true);*/
        // Start process with Java API and variables
        //ProcessApproveTweet
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 4 test - MANKOVSKYI");
        variables.put("approved", true);
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
        assertThat(processInstance).isWaitingAt("ReviewTweetTask");

        List<Task> taskList = taskService()
                .createTaskQuery()
                .taskCandidateGroup("management")
                .processInstanceId(processInstance.getId())
                .list();

        Task managementTask = taskQuery().taskCandidateGroup("management").singleResult();

        System.out.println(taskList);
        assertThat(managementTask).isNotNull();



//    complete(task(), withVariables("approved", true));
        Task task = taskList.get(0);
        Map<String, Object> approvedMap = new HashMap<String, Object>();
        approvedMap.put("approved", true);
        taskService().complete(task.getId(), approvedMap);

        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        Job job = jobList.get(0);
        Assertions.assertThat(jobList).hasSize(1);
        execute(job);

        assertThat(processInstance).hasPassed("IsApproveGateway");
        assertThat(processInstance).hasPassed("PublishTweetTask");
        assertThat(processInstance).hasNotPassed("DeclineTweetTask");

        assertThat(processInstance).variables().containsEntry("tweetId", 0L);

        // Make assertions on the process instance
        assertThat(processInstance).isEnded();

    }

    @Test
    @Deployment(resources = "twitter-project-mankovskyi.bpmn")
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
    @Deployment(resources = "twitter-project-mankovskyi.bpmn")
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
    @Deployment(resources = "twitter-project-mankovskyi.bpmn")
    public void testTweetWithdrawn_withMessaging() {

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("content", "Test tweetWithdrawn message - MANKOVSKYI");
        variables.put("approved", true);

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
        assertThat(processInstance).isStarted().isWaitingAt("ReviewTweetTask");

        runtimeService()
                .createMessageCorrelation("tweetWithdrawn")
                .processInstanceVariableEquals("content", "Test tweetWithdrawn message - MANKOVSKYI")
                .correlateWithResult();

        // Make assertions on the process instance
        assertThat(processInstance).isEnded();
    }

}
