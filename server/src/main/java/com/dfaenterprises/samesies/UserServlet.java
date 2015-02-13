package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.data.SsUser;
import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ari Weiland
 */
public class UserServlet extends HttpServlet {

    private DatastoreService datastore;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        datastore = DatastoreServiceFactory.getDatastoreService();

        // initialize users
        KeyRange keyRange = datastore.allocateIds("User", 2);
        Entity user1 = SsUser.makeUser(keyRange.getStart(), "ari@samesies.com", "samesies123", "Saint Paul, MN", "Ajawa",
                "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College."
        );
        datastore.put(user1);
        Entity user2 = SsUser.makeUser(keyRange.getEnd(), "luke@samesies.com", "samesies456", "Saint Paul, MN", "KoboldForeman",
                "Luke Gehman", 20, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2."
        );
        datastore.put(user2);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        log("Email: " + email);
        log("Password: " + password);
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "user", Query.FilterOperator.EQUAL, SsUser.makeUserField(email)));
        PreparedQuery pq = datastore.prepare(query);
        log("Count: " + pq.countEntities(FetchOptions.Builder.withDefaults()));
        resp.setContentType("application/json");
        for (Entity e : pq.asIterable()) {
            if (password.equals(e.getProperty("password"))) {
                String json = gson.toJson(e);
                log(json);
                resp.getWriter().println(json);
            }
        }
    }
}
