package com.dfaenterprises.samesies.apn;

import apns.ApnsException;
import apns.DefaultFeedbackService;
import apns.FailedDeviceToken;
import apns.FeedbackService;
import com.dfaenterprises.samesies.Constants;
import com.dfaenterprises.samesies.DS;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class ApnFeedbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DatastoreService ds = DS.getDS();
        List<FailedDeviceToken> failedTokens;
        FeedbackService fs = new DefaultFeedbackService();
        try {
            failedTokens = fs.read(ApnManager.getFeedbackConnection(Constants.IS_PROD));
            List<Key> pushKeys = new ArrayList<>();
            for (FailedDeviceToken token : failedTokens) {
                pushKeys.add(DS.getPush(ds, "ios", token.getDeviceToken()).toEntity().getKey());
            }
            ds.delete(pushKeys);
        } catch (ApnsException e) {
            throw new ServletException(e);
        }
    }

}
