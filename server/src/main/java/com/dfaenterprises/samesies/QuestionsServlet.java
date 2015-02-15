package com.dfaenterprises.samesies;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class QuestionsServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    public void init() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity initTest = new Entity("Question", "INIT_TEST");
        Query initQuery = new Query("Question").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.EQUAL, initTest.getKey())).setKeysOnly();
        if (datastore.prepare(initQuery).countEntities(FetchOptions.Builder.withLimit(1)) == 0) {
            datastore.put(initTest);
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
            for (int i = 0; i < questions.length; i++) {
                Entity e = new Entity("Question");
                e.setProperty("question", questions[i]);
                // TODO: categories?
                e.setProperty("category", "random");
                //initialize bots with questions to keep Q+A pairs together
                e.setUnindexedProperty("bot1", bot1[i]);
                datastore.put(e);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        resp.setContentType("application/json");
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.getWriter().println(")]}',");

        String queryType = req.getParameter("query");

        if (queryType.equals("all")) {
            String category = req.getParameter("category");
            List<String> questions;
            Query query = new Query("Question").addProjection(new PropertyProjection("question", String.class));
            if (!category.equals("all")) {
                query.setFilter(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, category));
            }
            PreparedQuery pq = datastore.prepare(query);
            questions = new ArrayList<String>();
            for (Entity e : pq.asIterable()) {
                questions.add((String) e.getProperty("question"));
            }
            String json = gson.toJson(questions.toArray());
            log(json);
            resp.getWriter().println(json);
        } else if (queryType.equals("episode")) {
            int count = Integer.parseInt(req.getParameter("count"));

            Query query = new Query("Question").setKeysOnly();
            PreparedQuery pq = datastore.prepare(query);

            int max = pq.countEntities(FetchOptions.Builder.withDefaults());
            int[] a = new int[max];
            for (int i=0; i<max; ++i) {
                a[i] = i;
            }
            int top = 0;
            while (top < count) {
                int swap = (int) (Math.random() * (max - top) + top);
                int tmp = a[swap];
                a[swap] = a[top];
                a[top] = tmp;
                top++;
            }

            List<Entity> keys = pq.asList(FetchOptions.Builder.withDefaults());
            JsonArray questions = new JsonArray();
            JsonArray bot = new JsonArray();
            try {
                for (int i=0; i< count; i++) {
                    Entity e = datastore.get(keys.get(i).getKey());
                    questions.add(new JsonPrimitive((String) e.getProperty("question")));
                    bot.add(new JsonPrimitive((String) e.getProperty("bot1")));
                }
            } catch (EntityNotFoundException e) {
                log("Retrieval error: ", e);
            }
            JsonObject output = new JsonObject();
            output.add("questions", questions);
            output.add("bot", bot);
            String json = gson.toJson(output);
            log(json);
            resp.getWriter().println(json);
        }
    }
}
