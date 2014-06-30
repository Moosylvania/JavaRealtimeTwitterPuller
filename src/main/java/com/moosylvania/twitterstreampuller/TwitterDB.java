/*
  */

package com.moosylvania.twitterstreampuller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import twitter4j.User;

/**
 *
 * Twitter DB - class to connect to a mysql database and store tweets.
 * All Posts are stored in a table called Twitter Posts.
 */
public class TwitterDB {

    private static String ip = "";
    private static String dbname = "";
    private static String username = "";
    private static String password = "";


    public TwitterDB(String ip, String username, String password, String dbname) {
        this.ip = ip;
        this.dbname = dbname;
        this.username = username;
        this.password = password;
    }
    protected static Connection connectDB() {
        Connection conn = null;

        String url = "jdbc:mysql://"+ip+"/"+dbname+"?allowMultiQueries=true";

        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            System.out.println(ex.getErrorCode() + " " + ex.getMessage());
        }

        return conn;
    }

    protected static void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public static void saveTweet(Long statusID, User user, Boolean retweet, String replyToScreenName, Date createdAt, String json) {
        Connection conn = connectDB();
        try {
            PreparedStatement pst = null;

            pst = conn.prepareStatement("INSERT INTO TwitterPost(TweetID, Username, Retweet, RepliedTo, Created, JsonValue) Values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            pst.setLong(1, statusID);

            pst.setString(2, user.getScreenName());

            pst.setBoolean(3, retweet);

            pst.setString(4, replyToScreenName);

            Integer theTime = (int) TimeUnit.MILLISECONDS.toSeconds(createdAt.getTime());;
            pst.setInt(5, theTime);

            pst.setString(6, json);

            pst.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public static void deleteTweet(Long statusID) {
        Connection conn = connectDB();
        try {
            PreparedStatement pst = null;

            pst = conn.prepareStatement("DELETE FROM TwitterPost WHERE TweetID = ?");
            pst.setLong(1, statusID);

            ResultSet rs = pst.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            closeConnection(conn);
        }
    }
}
