package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.*;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.*;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    public void init() {
        DatastoreService ds = getDS();
        // if this entity is in the ds, it has already been initialized, so don't init
        Entity initTest = new Entity("INIT", "INIT_TEST");
        if (!contains(ds, initTest)) {
            ds.put(initTest);

            // initialize users
            Entity user1 = new User("ari@samesies.com", "samesies123", "Saint Paul, MN", "Ajawa",
                    "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College."
            ).toEntity();
            ds.put(user1);
            Entity user2 = new User("luke@samesies.com", "samesies456", "Saint Paul, MN", "KoboldForeman",
                    "Luke Gehman", 21, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2."
            ).toEntity();
            ds.put(user2);

            // initialize friends
            Entity friendship = new Entity("Friend");
            friendship.setProperty("uid1", user1.getKey().getId());
            friendship.setProperty("uid2", user2.getKey().getId());
            ds.put(friendship);

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
                q.setCategory("Random");
                ds.put(q.toEntity());
            }
            for (Question q : questionsBad) {
                q.setCategory("Bad");
                ds.put(q.toEntity());
            }

            // initialize question categories
            ds.put(new Entity("Category", "All"));
            ds.put(new Entity("Category", "Random"));
            ds.put(new Entity("Category", "Bad"));
        }
    }

    @ApiMethod(name = "samesiesApi.login",
            path = "users/login",
            httpMethod = ApiMethod.HttpMethod.POST)
    public User login(User user) throws ServiceException {
        DatastoreService ds = getDS();

        String email = user.getEmail();
        String password = user.getPassword();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        Entity e = getUserByEmail(ds, email);
        if (e == null) {
            throw new NotFoundException("Invalid Email");
        } else if (password != null && password.equals(e.getProperty("password"))) {
            return new User(e, User.SELF);
        } else {
            throw new BadRequestException("Invalid Password");
        }
    }

    @ApiMethod(name = "samesiesApi.createUser",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.POST)
    public User createUser(User newUser) throws ServiceException {
        DatastoreService ds = getDS();

        String email = newUser.getEmail();
        String password = newUser.getPassword();
        String location = newUser.getLocation();
        String alias = newUser.getAlias();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        if (password == null) {
            throw new BadRequestException("Invalid Password");
        }
        if (location == null) {
            throw new BadRequestException("Invalid Location");
        }
        if (getUserByEmail(ds, email) == null) {
            if (alias == null) {
                newUser.setDefaultAlias();
            }
            newUser.setBlankQuestions();
            Entity e = newUser.toEntity();
            ds.put(e);
            newUser.setId(e.getKey().getId());
            return newUser;
        } else {
            throw new ForbiddenException("Email already in use");
        }
    }

    @ApiMethod(name = "samesiesApi.getUser",
            path = "users/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User getUser(@Named("id") long uid) throws ServiceException {
        return getUserById(getDS(), uid, User.STRANGER);
    }

    @ApiMethod(name = "samesiesApi.updateUser",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateUser(User user) throws ServiceException {
        DatastoreService ds = getDS();
        Entity entity = user.toEntity();
        if (!entity.getKey().isComplete()) {
            throw new BadRequestException("User ID not specified");
        }
        if (contains(ds, entity)) {
            ds.put(entity);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @ApiMethod(name = "samesiesApi.getFriends",
            path = "users/friends/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<User> getFriends(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = getDS();

        Query.Filter filter = new Query.CompositeFilter(Query.CompositeFilterOperator.OR, Arrays.asList(
                (Query.Filter) new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, uid),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, uid)
        ));
        Query query = new Query("Friend").setFilter(filter);
        PreparedQuery pq = ds.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            long uid1 = (long) e.getProperty("uid1");
            long uid2 = (long) e.getProperty("uid2");
            if (uid1 == uid) {
                users.add(getUserById(ds, uid2, User.FRIEND));
            } else {
                users.add(getUserById(ds, uid1, User.FRIEND));
            }
        }
        return users;
    }

    @ApiMethod(name = "samesiesApi.getCommunity",
            path = "communities/{location}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Community getCommunity(@Named("location") String location) throws ServiceException {
        // TODO: eventually need to be more clever about location stuff
        DatastoreService ds = getDS();
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "location", Query.FilterOperator.EQUAL, location));
        PreparedQuery pq = ds.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            users.add(new User(e, User.STRANGER));
        }
        return new Community(location, users);
    }

    @ApiMethod(name = "samesiesApi.getAllQuestions",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getAllQuestions() throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Question")
                .addProjection(new PropertyProjection("q", String.class))
                .addProjection(new PropertyProjection("category", String.class));
        PreparedQuery pq = ds.prepare(query);
        List<Question> questions = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            questions.add(new Question(e));
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.getQuestion",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Question getQuestion(@Named("id") long qid) throws ServiceException {
        return getQuestion(getDS(), qid);
    }

    @ApiMethod(name = "samesiesApi.getQuestions",
            path = "questions/list",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getQuestions(@Named("ids") long[] qids) throws ServiceException {
        DatastoreService ds = getDS();
        List<Question> questions = new ArrayList<>();
        for (long qid : qids) {
            questions.add(getQuestion(ds, qid));
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.getCategories",
            path = "categories",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<String> getCategories() throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Category").setKeysOnly();
        PreparedQuery pq = ds.prepare(query);
        List<String> categories = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            categories.add(e.getKey().getName());
        }
        return categories;
    }

    /**
     * Finds a new random episode.
     * If a currently matching episode matches with the user, matches the episode and returns it.
     * If none match, creates a new matching episode and returns that episode.
     * @param myUid
     * @return
     * @throws ServiceException
     */
    @ApiMethod(name = "samesiesApi.findEpisode",
            path = "episode/find/{myId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode findEpisode(@Named("myId") long myUid) throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Episode").setFilter(new Query.CompositeFilter(
                Query.CompositeFilterOperator.AND, Arrays.asList(
                (Query.Filter) new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, Episode.Status.MATCHING.name()),
                new Query.FilterPredicate("isPersistent", Query.FilterOperator.EQUAL, false)
        ))).addSort("startDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);

        Iterator<Entity> iter = pq.asIterator();
        Episode episode = null;
        while (episode == null && iter.hasNext()) {
            Episode temp = new Episode(iter.next());
            if (isMatch(myUid, temp)) {
                episode = temp;
            }
        }
        // TODO: handle categories
        if (episode == null) {
            episode = new Episode(myUid);
        } else {
            episode.setStatus(Episode.Status.IN_PROGRESS);
            episode.setUid2(myUid);
            episode.setQids(getEpisodeQs(ds, "Random"));
        }
        Entity e = episode.toEntity();
        ds.put(e);
        episode.setId(e.getKey().getId());
        return episode;
    }

    @ApiMethod(name = "samesiesApi.getEpisode",
            path = "episode/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Episode getEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = getDS();
        return getEpisode(ds, eid);
    }

    @ApiMethod(name = "samesiesApi.answerEpisode",
            path = "episode/answer/{id}/{myId}/{answer}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Episode answerEpisode(@Named("id") long eid, @Named("myId") long myUid, @Named("answer") String answer) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        boolean is1 = (myUid == episode.getUid1());
        List<String> answers = episode.getAnswers(is1);
        if (answers == null) {
            answers = new ArrayList<>();
        }
        answers.add(answer);
        episode.setAnswers(is1, answers);
        ds.put(episode.toEntity());
        return episode;
    }

    @ApiMethod(name = "samesiesApi.endEpisode",
            path = "episode/end/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void endEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        if (episode.getStatus() == Episode.Status.MATCHING) {
            episode.setStatus(Episode.Status.UNMATCHED);
        } else if (episode.getAnswers1().size() == 10 && episode.getAnswers2().size() == 10) {
            episode.setStatus(Episode.Status.COMPLETE);
        } else {
            episode.setStatus(Episode.Status.ABANDONED);
        }
        ds.put(episode.toEntity());
    }

    private static DatastoreService getDS() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private static boolean contains(DatastoreService ds, Entity entity) {
        try {
            ds.get(entity.getKey());
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * This method decides whether a user is compatible with a matching-state episode
     * @param uid
     * @param episode
     * @return
     */
    private static boolean isMatch(long uid, Episode episode) {
        // TODO: logic here
        return uid != episode.getUid1();
    }

    private static User getUserById(DatastoreService ds, long id, int type) throws NotFoundException {
        try {
            return new User(ds.get(KeyFactory.createKey("User", id)), type);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Email not found", e);
        }
    }

    private static Entity getUserByEmail(DatastoreService ds, String email) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = ds.prepare(query);

        return pq.asSingleEntity();
    }

    private static List<Long> getEpisodeQs(DatastoreService ds, String category) {
        Query query = new Query("Question").setFilter(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, category)).setKeysOnly();
        PreparedQuery pq = ds.prepare(query);

        int max = pq.countEntities(FetchOptions.Builder.withDefaults());
        int[] a = new int[max];
        for (int i=0; i<max; ++i) {
            a[i] = i;
        }
        int top = 0;
        while (top < 10) {
            int swap = (int) (Math.random() * (max - top) + top);
            int tmp = a[swap];
            a[swap] = a[top];
            a[top] = tmp;
            top++;
        }
        List<Entity> keys = pq.asList(FetchOptions.Builder.withDefaults());
        List<Long> questions = new ArrayList<>();
        for (int i=0; i<10; i++) {
            questions.add(keys.get(a[i]).getKey().getId());
        }
        return questions;
    }

    private static List<Long> getQids(List<Question> questions) {
        List<Long> qids = new ArrayList<>();
        for (Question q : questions) {
            qids.add(q.getId());
        }
        return qids;
    }

    private static Question getQuestion(DatastoreService ds, long qid) throws NotFoundException {
        try {
            return new Question(ds.get(KeyFactory.createKey("Question", qid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Question not found", e);
        }
    }

    private static Episode getEpisode(DatastoreService ds, long id) throws NotFoundException {
        try {
            return new Episode(ds.get(KeyFactory.createKey("Episode", id)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Episode not found", e);
        }
    }
}
