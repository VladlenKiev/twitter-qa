package com.camunda.training.delegate;

import com.camunda.training.service.CreateTweetService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class PublishTweetDelegate implements JavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final CreateTweetService createTweetService;

    public PublishTweetDelegate(CreateTweetService createTweetService) {
        this.createTweetService = createTweetService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String content = (String) delegateExecution.getVariable("content");
        LOGGER.info("content in delegate = {}", content);
//        String content = "I have done it 3! Mankovskyi!";

        Long tweetId = createTweetService.pushTweet(content + LocalDateTime.now().toString());
        delegateExecution.setVariable("tweetId",tweetId);
    }
}
