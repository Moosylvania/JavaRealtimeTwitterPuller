package com.moosylvania.twitterstreampuller;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.v3.Twitter4jStatusClient;
import com.twitter.hbc.twitter4j.v3.handler.StatusStreamHandler;
import com.twitter.hbc.twitter4j.v3.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.v3.message.StallWarningMessage;
import com.twitter.hbc.core.endpoint.Location;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import com.google.gson.Gson;

/**
 * Moosylvania Puller -
 * Implements handling Of Tweets from twitters HBC Clients
 */
public class MoosylvaniaPuller {

    private static TwitterDB thedb;
    private int pullType;

    // A bare bones StatusStreamHandler, which extends listener and gives some extra functionality
    private StatusListener listener = new StatusStreamHandler() {

        @Override
        public void onStatus(Status status) {
            System.out.println("Saving Tweet : TweetID - " + String.valueOf(status.getId()) + " From - " + status.getUser().getScreenName());
            Gson gson = new Gson();
            String json = gson.toJson(status);
            thedb.saveTweet(status.getId(), status.getUser(), status.isRetweet(), status.getInReplyToScreenName(), status.getCreatedAt(), json);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            thedb.deleteTweet(statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int limit) {
        }

        @Override
        public void onScrubGeo(long user, long upToStatus) {
        }

        @Override
        public void onStallWarning(StallWarning warning) {
        }

        @Override
        public void onException(Exception e) {
            System.out.println(e.getMessage());
        }

        @Override
        public void onDisconnectMessage(DisconnectMessage message) {
            System.out.println(message.getDisconnectReason());
        }

        @Override
        public void onStallWarningMessage(StallWarningMessage warning) {
        }

        @Override
        public void onUnknownMessageType(String s) {
            System.out.println(s);
        }
    };

    public void oauth(String consumerKey, String consumerSecret, String token, String secret, TwitterEndpoint endpoint) throws InterruptedException {
        // Create an appropriately sized blocking queue
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

        // Create a new BasicClient. By default gzip is enabled.
        BasicClient client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Create an executor service which will spawn threads to do the actual work of parsing the incoming messages and
        // calling the listeners on each message
        int numProcessingThreads = 4;
        ExecutorService service = Executors.newFixedThreadPool(numProcessingThreads);

        // Wrap our BasicClient with the twitter4j client
        Twitter4jStatusClient t4jClient = new Twitter4jStatusClient(
                client, queue, Lists.newArrayList(listener), service);

        // Establish a connection
        t4jClient.connect();
        for (int threads = 0; threads < numProcessingThreads; threads++) {
            // This must be called once per processing thread
            t4jClient.process();
        }
    }

    /*
     @input args[] =
     0 = consumer.key
     1 = consumer.secret
     2 = access.token
     3 = access.token.secret
     4 = DB IP
     5 = DB Username
     6 = DB Password
     7 = DB Name
     8 = twitter.streamid (user id or hashtag/phrase depending on #8
     9 = PullUser - Integer - 1 = pull user, 2 = track users, anything else = track location
     */
    public static void main(String[] args) {
        try {
            System.out.println("Setting up TwitterListener to listen for '" + args[8] + "'");
            MoosylvaniaPuller mp = new MoosylvaniaPuller();

            TwitterEndpoint endpoint = new TwitterEndpoint();

            mp.pullType = Integer.parseInt(args[9]);
            if (mp.pullType == 1) {
                long thelong = Long.parseLong(args[8]);
                List<Long> thelonglist = new ArrayList<Long>();
                thelonglist.add(0, thelong);
                endpoint.followings(thelonglist);
            } else if(mp.pullType == 2){
                List<String> theFilters = Arrays.asList(args[8].split("\\s*,\\s*"));
                endpoint.trackTerms(theFilters);
            } else {
                List<String> coords = Arrays.asList(args[8].split("\\s*,\\s*"));
                Location.Coordinate coord1 = new Location.Coordinate(Double.parseDouble(coords.get(0)),Double.parseDouble(coords.get(1)));
                Location.Coordinate coord2 = new Location.Coordinate(Double.parseDouble(coords.get(2)),Double.parseDouble(coords.get(3)));
                Location theloc = new Location(coord1,coord2);
                List<Location> loclist = new ArrayList<Location>();

                loclist.add(theloc);

                endpoint.locations(loclist);
            }

            thedb = new TwitterDB(args[4], args[5], args[6], args[7]);
            mp.oauth(args[0], args[1], args[2], args[3], endpoint);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
}
