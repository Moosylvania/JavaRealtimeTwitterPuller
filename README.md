JavaRealtimeTwitterPuller
=========================

Java Application utilizing Twitters HBC to pull in real time content.

## Dependencies
1. Java 1.6+ preferably 1.7
2. Apache Maven 2.x + preverably 3.2+
3. MySQL Database

## Setup and Installation
1. Clone repository to your server
2. Create an application on http://dev.twitter.com and store your oauth keys
3. Run the command >

        mvn install
        
4. This will execute and pull all required java dependencies.

## Execution of JavaRealtimeTwitterPuller

This application currently has a series of 10 command line arguments
1. consumer.key
2. consumer.secret
3. access.token
4. access.token.secret
5. DB IP
6. DB Username
7. DB Password
8. DB Name
9. twitter.streamid (user id or hashtag/phrase(s) or Location (lat,long))
10. PullUser - Integer - 1 = pull user, 2 = track users, anything else = track location

Example command - to track a user:

        mvn exec:java -pl com.moosylvania:JavaRealtimeTwitterPuller -Dconsumer.key=XXX -Dconsumer.secret=YYY -Daccess.token=ZZZ -Daccess.token.secret=123 -Ddb.ip=127.0.0.1 -Ddb.username=username -Ddb.password=password -Ddb.dbname=dbname -Dtwitter.streamid=UserIdNumber -Dtwitter.pulluser=1 > /var/log/twitterpull.log 2>&1
