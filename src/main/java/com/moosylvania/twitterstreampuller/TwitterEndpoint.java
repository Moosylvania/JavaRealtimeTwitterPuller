/*
 */
package com.moosylvania.twitterstreampuller;

import com.google.common.base.Joiner;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.endpoint.DefaultStreamingEndpoint;
import com.twitter.hbc.core.endpoint.Location;
import java.util.List;

/**
 *
 * Twitter Endpoint - implements the Endpoint that twitter HBC posts to.
 */
public class TwitterEndpoint extends DefaultStreamingEndpoint {

    public static final String PATH = "/statuses/filter.json";

    public TwitterEndpoint() {
        this(false);
    }

    /**
     * @param backfillable set to true if you have elevated access
     */
    public TwitterEndpoint(boolean backfillable) {
        super(PATH, HttpConstants.HTTP_POST, backfillable);
    }

    public TwitterEndpoint followings(List<Long> userIds) {
        addPostParameter(Constants.FOLLOW_PARAM, Joiner.on(',').join(userIds));
        return this;
    }

    /**
     * @param terms a list of Strings to track. These strings should NOT be
     * url-encoded.
     */
    public TwitterEndpoint trackTerms(List<String> terms) {
        addPostParameter(Constants.TRACK_PARAM, Joiner.on(',').join(terms));
        return this;
    }

    public TwitterEndpoint locations(List<Location> locations) {
        addPostParameter(Constants.LOCATION_PARAM, Joiner.on(',').join(locations));
        return this;
    }
}
