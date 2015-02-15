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

    private final Gson gson = new Gson();

    @Override
    public void init() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // if this entity is in the datastore, it has already been initialized, so don't init
        Entity initTest = new Entity("User", "INIT_TEST");
        Query initQuery = new Query("User").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                        Query.FilterOperator.EQUAL, initTest.getKey())).setKeysOnly();
        if (datastore.prepare(initQuery).countEntities(FetchOptions.Builder.withLimit(1)) == 0) {
            datastore.put(initTest);

            // initialize users
            Entity user1 = SsUser.makeUser("ari@samesies.com", "samesies123", "Saint Paul, MN", "Ajawa",
                    "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College."
            );
            datastore.put(user1);
            Entity user2 = SsUser.makeUser("luke@samesies.com", "samesies456", "Saint Paul, MN", "KoboldForeman",
                    "Luke Gehman", 20, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2."
            );
            datastore.put(user2);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        resp.setContentType("application/json");
        resp.addHeader("Access-Control-Allow-Origin", "*");

        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String location = req.getParameter("location");
        String alias = req.getParameter("alias");

        Entity e = getUserByEmail(datastore, email);
        if (e == null) {
            Entity newUser = SsUser.makeUser(email, password, location, alias);
            datastore.put(newUser);
            resp.getWriter().println(")]}',");
            resp.getWriter().println(SsUser.toJson(newUser, gson, false));
        } else { // that email is already in the system
            resp.sendError(403, "Forbidden: Email already in use");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        resp.setContentType("application/json");
        resp.addHeader("Access-Control-Allow-Origin", "*");

        String queryType = req.getParameter("query");


        if (queryType.equals("login")) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            log("Email: " + email);
            log("Password: " + password);

            Entity e = getUserByEmail(datastore, email);
            if (e == null) {
                resp.sendError(404, "Not Found: Email not found");
            } else if (password.equals(e.getProperty("password"))) {
                String json = SsUser.toJson(e, gson, false);
                log(json);
                resp.getWriter().println(")]}',");
                resp.getWriter().println(json);
            } else {
                resp.sendError(400, "Bad Request: Invalid Password");
            }
        } else if (queryType.equals("email")) {
            String email = req.getParameter("email");
            log("Email: " + email);
            Entity e = getUserByEmail(datastore, email);
            String json = SsUser.toJson(e, gson, true);
            log(json);
            resp.getWriter().println(json);
        } else if (queryType.equals("id")) {
            long id = Long.parseLong(req.getParameter("id"));
            log("ID: " + id);
            try {
                Entity e = datastore.get(KeyFactory.createKey("User", id));
                String json = SsUser.toJson(e, gson, true);
                log(json);
                resp.getWriter().println(")]}',");
                resp.getWriter().println(json);
            } catch (EntityNotFoundException e) {
                log("Retrieval error", e);
            }
        } else if (queryType.equals("location")) {
            // TODO: eventually need to be more clever about location stuff
            String location = req.getParameter("location");
            log("Location: " + location);
            Query query = new Query("User").setFilter(new Query.FilterPredicate(
                    "location", Query.FilterOperator.EQUAL, location));
            PreparedQuery pq = datastore.prepare(query);

            log("Count: " + pq.countEntities(FetchOptions.Builder.withDefaults()));

            String json = SsUser.toJson(pq.asIterable(), gson, true);
            log(json);
            resp.getWriter().println(")]}',");
            resp.getWriter().println(json);
        }
    }

    private Entity getUserByEmail(DatastoreService datastore, String email) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = datastore.prepare(query);

        return pq.asSingleEntity();
    }
}
