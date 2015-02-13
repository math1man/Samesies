package com.dfaenterprises.samesies;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ari Weiland
 */
public class QuestionsServlet extends HttpServlet {

    private DatastoreService datastore;

    @Override
    public void init() {
        datastore = DatastoreServiceFactory.getDatastoreService();

        // initialize questions
        String[] questions = {
                "How do you feel about long walks on the beach?",
                "What is your favorite type of music?",
                "What do you like to do on a first date?",
                "What is your go-to conversation starter?",
                "If you had to take a date to dinner, where would you go?",
                "If you could have a superpower, what would it be and why?"
        };
        String[] bot1 = {
                "I prefer to go swimming than to walk.",
                "Anything they play on the radio is fine with me.",
                "Dinner and a movie.  Call me old fashioned if you like.",
                "Have you heard about what happened to Pluto? Shame...",
                "I would go to Pad Thai on Grand Ave.",
                "I would be able to teleport.  That way I could avoid the cold."
        };
        for (int i=0; i<questions.length; i++) {
            Entity e = new Entity("Question");
            e.setProperty("question", questions[i]);
            e.setProperty("value", Math.random());
            //initialize bots with questions to keep Q+A pairs together
            e.setUnindexedProperty("bot1", bot1[i]);
            datastore.put(e);
        }

        // any other initialization
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("{ \"name\": \"User\" }");
    }
}
