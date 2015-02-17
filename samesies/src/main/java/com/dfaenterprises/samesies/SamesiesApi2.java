package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.Episode;
import com.dfaenterprises.samesies.model.Question;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.*;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
@Api(
        name = "samesies",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class SamesiesApi2 {

    public SamesiesApi2() {

    }

    public void init() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // if this entity is in the datastore, it has already been initialized, so don't init
        Entity initTest = new Entity("INIT", "INIT_TEST");
        Query initQuery = new Query("INIT").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
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

    public Entity login(@Named("email") String email, @Named("password") String password) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity e = getUserByEmail(datastore, email);
        if (e == null) {
            throw new NotFoundException("Email not found");
        } else if (password.equals(e.getProperty("password"))) {
            return e;
        } else {
            throw new BadRequestException("Invalid password");
        }
    }

//    @ApiMethod(name = "samesiesApi.userEmail")
//    public Entity getUserByEmail(@Named("email") String email) throws ServiceException {
//        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//
//        Entity e = getUserByEmail(datastore, email);
//        if (e == null) {
//            throw new NotFoundException("Email not found");
//        } else {
//            return e;
//        }
//    }
//
//    @ApiMethod(name = "samesiesapi.userId")
//    public Entity getUserById(@Named("id") long id) throws ServiceException {
//        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//
//        try {
//            return datastore.get(KeyFactory.createKey("User", id));
//        } catch (EntityNotFoundException e1) {
//            throw new NotFoundException("Email not found", e1);
//        }
//    }
//
//    @ApiMethod(name = "samesiesapi.usersLocation")
//    public Collection<Entity> getUsersByLocation(@Named("location") String location) throws ServiceException {
//        // TODO: eventually need to be more clever about location stuff
//        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//        Query query = new Query("User").setFilter(new Query.FilterPredicate(
//                "location", Query.FilterOperator.EQUAL, location));
//        PreparedQuery pq = datastore.prepare(query);
//        return pq.asList(FetchOptions.Builder.withDefaults());
//    }
//
//    @ApiMethod(name = "samesiesApi.userEmail")
//    public Entity createAccount(@Named("email") String email, @Named("password") String password,
//                                @Named("location") String location, @Nullable @Named("alias") String alias) throws ServiceException {
//        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//
//        Entity e = getUserByEmail(datastore, email);
//        if (e == null) {
//            throw new NotFoundException("Email not found");
//        } else {
//            return e;
//        }
//    }

    @ApiMethod(name = "samesiesApi.questions")
    public List<Question> getQuestions(@Nullable @Named("category") String category) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Question").addProjection(new PropertyProjection("question", String.class));
        if (category != null && !category.equals("all")) {
            query.setFilter(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, category));
        }
        PreparedQuery pq = datastore.prepare(query);
        List<Question> questions = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            questions.add(new Question((String) e.getProperty("question")));
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.makeEpisode")
    public Episode makeEpisode(@Named("count") int count) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
        Question[] questions = new Question[count];
        try {
            for (int i=0; i<count; i++) {
                Entity e = datastore.get(keys.get(a[i]).getKey());
                questions[i] = new Question((String) e.getProperty("question"), (String) e.getProperty("bot1"));
            }
            return new Episode(questions);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Not Found", e);
        }
    }

    private Entity getUserByEmail(DatastoreService datastore, String email) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = datastore.prepare(query);

        return pq.asSingleEntity();
    }
}
