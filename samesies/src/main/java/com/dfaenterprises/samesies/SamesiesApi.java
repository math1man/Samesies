package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.*;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.*;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
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
public class SamesiesApi {

    // defaults to POST (not relevant though)
    public void init() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // if this entity is in the datastore, it has already been initialized, so don't init
        Entity initTest = new Entity("INIT", "INIT_TEST");
        Query initQuery = new Query("INIT").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.EQUAL, initTest.getKey())).setKeysOnly();
        if (datastore.prepare(initQuery).countEntities(FetchOptions.Builder.withLimit(1)) == 0) {
            datastore.put(initTest);

            // initialize users
            Entity user1 = new User("ari@samesies.com", "samesies123", "Saint Paul, MN", "Ajawa",
                    "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College."
            ).toEntity();
            datastore.put(user1);
            Entity user2 = new User("luke@samesies.com", "samesies456", "Saint Paul, MN", "KoboldForeman",
                    "Luke Gehman", 21, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2."
            ).toEntity();
            datastore.put(user2);

            // initialize friends
            Entity friendship = new Entity("Friend");
            friendship.setProperty("uid1", user1.getKey().getId());
            friendship.setProperty("uid2", user2.getKey().getId());
            datastore.put(friendship);

            // initialize questions
            Question[] questions = {
                    new Question("What do you like to do on a first date?",
                            "Dinner and a movie.  Call me old fashioned if you like."),
                    new Question("What is your go-to conversation starter?",
                            "Have you heard about what happened to Pluto? Shame..."),
                    new Question("If you had to take a date to dinner, where would you go?",
                            "I would go to Pad Thai on Grand Ave."),
                    new Question("If you could have a superpower, what would it be and why?",
                            "I would be able to teleport.  That way I could avoid the cold."),
                    new Question("Given the choice of anyone in the world, whom would you want as a dinner guest?",
                            "I think it would be really interesting to pick Albert Einstein's brain."),
                    new Question("Would you like to be famous? In what way?",
                            "I just want to do something notable with my life, like discover something significant."),
                    new Question("Before making a telephone call, do you ever rehearse what you are going to say and why?",
                            "Only if I'm nervous about the call."),
                    new Question("What would constitute a \"perfect\" day for you?",
                            "No obligations, just relaxing and having fun."),
                    new Question("When did you last sing to yourself? To someone else?",
                            "I on occasion sing to myself in the shower."),
                    new Question("If you were able to live to the age of 90 and retain either the mind or body of a " +
                            "30-year-old for the last 60 years of your life, which would you want and why?",
                            "Probably the mind of a 30 year old. I really value my mental acuity."),
                    new Question("For what in your life do you feel most grateful?",
                            "My family. I love them a lot."),
                    new Question("If you could change anything about the way you were raised, what would it be?",
                            "Sometimes I wish I was raised in a different part of the country."),
                    new Question("If you could wake up tomorrow having gained any one quality or ability, what would it be?",
                            "I would love to be better at playing the piano than I am."),
                    new Question("If a crystal ball could tell you the truth about yourself, your life, the future " +
                            "or any one thing, what would you want to know?",
                            "I would ask it what I should focus my energy on most to be happiest."),
                    new Question("Is there something that you've dreamed of doing for a long time? Why haven't you done it?",
                            "I've kind of always wanted to go skydiving, but the opportunity just never came up."),
                    new Question("What is the greatest accomplishment of your life?",
                            "When I finish this app, it will probably be it."),
                    new Question("What do you value most in a friendship?",
                            "Being able to talk about anything, free of judgment."),
                    new Question("What is your most treasured memory?",
                            "I have a memory of the night my brother was born. It probably isn't real, but I still treasure it."),
                    new Question("What is your most terrible memory?",
                            "They aren't exactly memories, but some things that I have done while drunk I really regret."),
                    new Question("If you knew that in one year you would die suddenly, would you change anything " +
                            "about the way you are now living and why?",
                            "Not really. I am fairly happy with my life."),
                    new Question("When did you last cry in front of another person? By yourself?",
                            "I cried by myself when my grandmother died last May. I have no idea the last time I " +
                            "cried in front of another person."),
                    new Question("What, if anything, is too serious to be joked about?",
                            "I feel like there is definitely a line, but I can't think of what's across it."),
                    new Question("If you were to die this evening with no opportunity to communicate with anyone, " +
                            "what would you most regret not having told someone? Why haven't you told them yet?",
                            "I'd probably regret not having talked to my parents in a few weeks."),
                    new Question("Your house, containing everything you own, catches fire. After saving your loved ones and " +
                            "pets, you have time to safely make a final dash to save any one item. What would it be and why?",
                            "Obviously my computer. Basically my entire life is on that thing."),
                    new Question("What is the most important thing your close friends should know about you?",
                            "I sometimes can be a bit of an asshole."),
                    new Question("What is one of your most embarrassing moments?",
                            "The one time I made out with my ex-girlfriend.")
            };
            Question[] questionsBad = {
                    new Question("How do you feel about long walks on the beach?",
                            "I prefer to go swimming than to walk."),
                    new Question("What is your favorite type of music?",
                            "Anything they play on the radio is fine with me."),
                    new Question("Complete the sentence \"I wish I had someone with whom I could share...\""),
                    new Question("Do you have a secret hunch about how you will die?"),
                    new Question("How do you feel about your relationship with your mother?"),
                    new Question("What does friendship mean to you?"),
                    new Question("What roles do love and affection play in your life?"),
                    new Question("How close and warm is your family? Do you feel your childhood was happier than most other people's?"),
                    new Question("Of all the people in your family, whose death would you find most disturbing? Why?")
            };
            for (Question q : questions) {
                Entity e = new Entity("Question");
                e.setProperty("question", q.getQ());
                e.setUnindexedProperty("answer", q.getA());
                // TODO: categories?
                e.setProperty("category", "Random");
                datastore.put(e);
            }
            for (Question q : questionsBad) {
                Entity e = new Entity("Question");
                e.setProperty("question", q.getQ());
                // TODO: categories?
                e.setProperty("category", "Bad");
                datastore.put(e);
            }

            // initialize question categories
            datastore.put(new Entity("Category", "All"));
            datastore.put(new Entity("Category", "Random"));
            datastore.put(new Entity("Category", "Bad"));
        }
    }

    // defaults to POST
    public User login(@Named("email") String email, @Named("password") String password) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity e = getUserByEmail(datastore, email);
        if (e == null) {
            throw new NotFoundException("Invalid Email");
        } else if (password.equals(e.getProperty("password"))) {
            return new User(e, User.SELF);
        } else {
            throw new BadRequestException("Invalid Password");
        }
    }

    @ApiMethod(name = "samesiesApi.update")
    public void updateUser(User user) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = user.toEntity();
        if (!entity.getKey().isComplete()) {
            throw new BadRequestException("User ID not specified");
        }
        try {
            datastore.get(entity.getKey());
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("User not found", e);
        }
        // the try-catch guarantees the entity is in the datastore
        datastore.put(entity);
    }

    @ApiMethod(name = "samesiesApi.create") // POST
    public User createAccount(@Named("email") String email, @Named("password") String password,
                              @Named("location") String location, @Nullable @Named("alias") String alias) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        if (getUserByEmail(datastore, email) == null) {
            Entity user = new User(email, password, location, alias).toEntity();
            datastore.put(user);
            return new User(user, User.SELF);
        } else {
            throw new ForbiddenException("Email already in use");
        }
    }

    @ApiMethod(name = "samesiesApi.user") // GET
    public User getUser(@Named("id") long id) throws ServiceException {
        return getUserById(id, User.STRANGER);
    }

    @ApiMethod(name = "samesiesApi.friends") // GET
    public FriendsList getFriends(@Named("id") long id) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter filter = new Query.CompositeFilter(Query.CompositeFilterOperator.OR, Arrays.asList(
                (Query.Filter) new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, id),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, id)
        ));
        Query query = new Query("Friend").setFilter(filter);
        PreparedQuery pq = datastore.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            long uid1 = (long) e.getProperty("uid1");
            long uid2 = (long) e.getProperty("uid2");
            if (uid1 == id) {
                users.add(getUserById(uid2, User.FRIEND));
            } else {
                users.add(getUserById(uid1, User.FRIEND));
            }
        }
        return new FriendsList(id, users);
    }

    @ApiMethod(name = "samesiesApi.community") // GET
    public Community getCommunity(@Named("location") String location) throws ServiceException {
        // TODO: eventually need to be more clever about location stuff
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "location", Query.FilterOperator.EQUAL, location));
        PreparedQuery pq = datastore.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            users.add(new User(e, User.STRANGER));
        }
        return new Community(location, users);
    }

    @ApiMethod(name = "samesiesApi.questions") // GET
    public List<Question> getQuestions() throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Question")
                .addProjection(new PropertyProjection("question", String.class))
                .addProjection(new PropertyProjection("category", String.class));
        PreparedQuery pq = datastore.prepare(query);
        List<Question> questions = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            questions.add(new Question((String) e.getProperty("question"), null, (String) e.getProperty("category")));
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.categories") // GET
    public Categories getCategories() throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Category").setKeysOnly();
        PreparedQuery pq = datastore.prepare(query);
        List<String> categories = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            categories.add(e.getKey().getName());
        }
        return new Categories(categories);
    }

    // TODO: this is temporary, needs to eventually be more complex (matching and all)
    @ApiMethod(name = "samesiesApi.makeEpisode") // POST
    public Episode makeEpisode(@Named("count") int count) throws ServiceException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Question").setFilter(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, "Random")).setKeysOnly();
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
                questions[i] = new Question((String) e.getProperty("question"), (String) e.getProperty("answer"));
            }
            return new Episode(questions);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Not Found", e);
        }
    }

    private User getUserById(long id, int type) throws NotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            return new User(datastore.get(KeyFactory.createKey("User", id)), type);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Email not found", e);
        }
    }

    private Entity getUserByEmail(DatastoreService datastore, String email) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = datastore.prepare(query);

        return pq.asSingleEntity();
    }
}
