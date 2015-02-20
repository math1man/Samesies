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
import java.util.Date;
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

    public void init() throws ServiceException {
        DatastoreService ds = getDS();
        // if this entity is in the ds, it has already been initialized, so don't init
        Entity initTest = new Entity("INIT", "INIT_TEST");
        if (!contains(ds, initTest)) {
            ds.put(initTest);

            // initialize users
            User user1 = new User("ari@samesies.com", "samesies123", "Saint Paul, MN", "Ajawa",
                    "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College.");
            Utils.put(ds, user1);
            User user2 = new User("luke@samesies.com", "samesies456", "Saint Paul, MN", "KoboldForeman",
                    "Luke Gehman", 21, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2.");
            Utils.put(ds, user2);

            // initialize friends
            Entity friendship = new Entity("Friend");
            friendship.setProperty("uid1", user1.getId());
            friendship.setProperty("uid2", user2.getId());
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
                Utils.put(ds, q);
            }
            for (Question q : questionsBad) {
                q.setCategory("Bad");
                Utils.put(ds, q);
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
        User dsUser = getUserByEmail(ds, email);
        if (dsUser == null) {
            throw new NotFoundException("Invalid Email");
        } else if (password != null && password.equals(dsUser.getPassword())) {
            return dsUser;
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
            Utils.put(ds, newUser);
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
        if (user.getId() == null) {
            throw new BadRequestException("User ID not specified");
        }
        if (contains(ds, user)) {
            Utils.put(ds, user);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @ApiMethod(name = "samesiesApi.getFriends",
            path = "users/friends/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<User> getFriends(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Friend").setFilter(Utils.makeDoubleFilter(Query.CompositeFilterOperator.OR,
                "uid1", Query.FilterOperator.EQUAL, uid, "uid2", Query.FilterOperator.EQUAL, uid));
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

        Query query = new Query("Episode").setFilter(Utils.makeDoubleFilter(Query.CompositeFilterOperator.AND,
                        "status", Query.FilterOperator.EQUAL, Episode.Status.MATCHING.name(),
                        "isPersistent", Query.FilterOperator.EQUAL, false)
                ).addSort("startDate", Query.SortDirection.ASCENDING);
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
            // rather than order episode uids, makes more sense to have uid1 be
            // the initiator and uid2 be the responder, for record-keeping's sake
            episode = new Episode(myUid);
        } else {
            episode.setStatus(Episode.Status.IN_PROGRESS);
            episode.setUid2(myUid);
            // get set of questions
            Query qQuery = new Query("Question").setFilter(new Query.FilterPredicate(
                    "category", Query.FilterOperator.EQUAL, "Random")).setKeysOnly();
            PreparedQuery qpq = ds.prepare(qQuery);
            int max = qpq.countEntities(FetchOptions.Builder.withDefaults());
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
            List<Entity> keys = qpq.asList(FetchOptions.Builder.withDefaults());
            List<Long> questions = new ArrayList<>();
            for (int i=0; i<10; i++) {
                questions.add(keys.get(a[i]).getKey().getId());
            }
            episode.setQids(questions);
        }
        Utils.put(ds, episode);
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
        Utils.put(ds, episode);
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
        Utils.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.startChat",
            path = "chat/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Chat startChat(@Named("myId") long myUid, @Named("theirId") long theirUid) throws ServiceException {
        DatastoreService ds = getDS();
        long[] uids = orderIds(myUid, theirUid); // avoid parity issues to simplify search
        Chat chat = getChat(ds, uids); // make sure a chat between these users doesn't already exist
        if (chat == null) {
            chat = new Chat(uids[0], uids[1]);
            Utils.put(ds, chat);
            return chat;
        } else {
            return chat;
        }
    }

    @ApiMethod(name = "samesiesApi.getChat",
            path = "chat/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Chat getChat(@Named("id") long cid) throws ServiceException {
        return getChat(getDS(), cid);
    }

    @ApiMethod(name = "samesiesApi.sendMessage",
            path = "message/{chatId}/{myId}/{message}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Message sendMessage(@Named("chatId") long cid, @Named("myId") long myUid, @Named("message") String message) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, cid);
        chat.modify();
        Utils.put(ds, chat);
        // TODO: maybe try to include a check to stop double-sending???
        Message m = new Message(cid, myUid, message);
        Utils.put(ds, m);
        return m;
    }

    @ApiMethod(name = "samesiesApi.getMessages",
            path = "message/{chatId}/{after}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Message> getMessages(@Named("chatId") long cid, @Named("after") Date after) throws ServiceException {
        DatastoreService ds = getDS();
        Query query = new Query("Message").setFilter(Utils.makeDoubleFilter(Query.CompositeFilterOperator.AND,
                "chatId", Query.FilterOperator.EQUAL, cid, "sentDate", Query.FilterOperator.GREATER_THAN, after))
                .addSort("sentDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);
        List<Message> messages = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            messages.add(new Message(e));
        }
        return messages;
    }

    private static DatastoreService getDS() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private static boolean contains(DatastoreService ds, Storable s) {
        return contains(ds, s.toEntity());
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

    private static User getUserByEmail(DatastoreService ds, String email) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = ds.prepare(query);

        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new User(e, User.SELF);
        }
    }

    private static Question getQuestion(DatastoreService ds, long qid) throws NotFoundException {
        try {
            return new Question(ds.get(KeyFactory.createKey("Question", qid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Question not found", e);
        }
    }

    private static Episode getEpisode(DatastoreService ds, long eid) throws NotFoundException {
        try {
            return new Episode(ds.get(KeyFactory.createKey("Episode", eid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Episode not found", e);
        }
    }

    private static Chat getChat(DatastoreService ds, long cid) throws NotFoundException {
        try {
            return new Chat(ds.get(KeyFactory.createKey("Chat", cid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Chat not found", e);
        }
    }

    private static Chat getChat(DatastoreService ds, long[] uids) {
        Query query = new Query("Chat").setFilter(Utils.makeDoubleFilter(Query.CompositeFilterOperator.AND,
                "uid1", Query.FilterOperator.EQUAL, uids[0], "uid2", Query.FilterOperator.EQUAL, uids[1]));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new Chat(e);
        }
    }

    private static long[] orderIds(long id1, long id2) throws BadRequestException {
        if (id1 == id2) {
            throw new BadRequestException("IDs must be different");
        } else if (id1 < id2) {
            return new long[]{id1, id2};
        } else {
            return new long[]{id2, id1};
        }
    }
}
