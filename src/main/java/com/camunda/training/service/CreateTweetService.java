package com.camunda.training.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Service
public class CreateTweetService {
    private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetService.class.getName());
//https://mobile.twitter.com/camunda_demo
    public Long pushTweet(String content) throws Exception {
        //Twitter does not support duplicate! just change content!
        LOGGER.info("Publishing tweet: " + content);
        AccessToken accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
        twitter.setOAuthAccessToken(accessToken);

        Status status = null;
        try {
            status = twitter.updateStatus(content);
        } catch (TwitterException te){
            throw new BpmnError("DuplicateTweet");
        }
        return status.getId();
    }
}
