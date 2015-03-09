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
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Named;
import java.util.*;

/**
 * @author Ari Weiland
 */
@Api(
        name = "samesies",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class SamesiesApi {

    public void initQuestions() throws ServiceException {
        DatastoreService ds = getDS();

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
        for (Question q : questions) {
            q.setCategory("Random");
            EntityUtils.put(ds, q);
        }

        ds.put(new Entity("Category", "All"));
        ds.put(new Entity("Category", "Random"));
    }

    public void initUsers() throws ServiceException {
        DatastoreService ds = getDS();
        User user1 = new User("ari@samesies.org", "samesies123", "Saint Paul, MN", "Ajawa",
                "Ari Weiland", 20, "Male", "I am a junior Physics and Computer Science major at Macalester College.");
        EntityUtils.put(ds, user1);
        User user2 = new User("luke@samesies.org", "samesies456", "Saint Paul, MN", "KoboldForeman",
                "Luke Gehman", 21, "Male", "I am a junior Biology major at Macalester College. I play a lot of Dota 2.");
        EntityUtils.put(ds, user2);

        Friend friend = new Friend(user1.getId(), user2.getId(), Friend.Status.ACCEPTED);
        EntityUtils.put(ds, friend);
    }

    public void initModes() throws ServiceException {
        DatastoreService ds = getDS();
        ds.put(new Entity("Mode", "Random"));
        ds.put(new Entity("Mode", "Personal"));
    }

    //----------------------------
    //        User Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.login",
            path = "user/login",
            httpMethod = ApiMethod.HttpMethod.POST)
    public User login(User user) throws ServiceException {
        DatastoreService ds = getDS();

        String email = user.getEmail();
        String password = user.getPassword();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        User dsUser = getUserByEmail(ds, email, User.Relation.SELF);
        if (dsUser == null) {
            throw new NotFoundException("Invalid Email");
        } else if (password != null && BCrypt.checkpw(password, dsUser.getHashedPw())) {
            return dsUser;
        } else {
            throw new BadRequestException("Invalid Password");
        }
    }

    @ApiMethod(name = "samesiesApi.createUser",
            path = "user/create",
            httpMethod = ApiMethod.HttpMethod.POST)
    public User createUser(User newUser) throws ServiceException {
        DatastoreService ds = getDS();

        String email = newUser.getEmail();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        if (newUser.getPassword() == null) {
            throw new BadRequestException("Invalid Password");
        }
        if (newUser.getLocation() == null) {
            throw new BadRequestException("Invalid Location");
        }
        if (getUserByEmail(ds, email, User.Relation.SELF) == null) {
            if (newUser.getAlias() == null) {
                newUser.setDefaultAlias();
            }
            newUser.setBlankQuestions();
            EntityUtils.put(ds, newUser);
            return newUser;
        } else {
            throw new ForbiddenException("Email already in use");
        }
    }

    @ApiMethod(name = "samesiesApi.getUser",
            path = "user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User getUser(@Named("id") long uid) throws ServiceException {
        return getUserById(getDS(), uid, User.Relation.STRANGER);
    }

    @ApiMethod(name = "samesiesApi.getUsers",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<User> getUsers(@Named("ids") long[] uids) throws ServiceException {
        List<Key> keys = new ArrayList<>();
        for (long uid : uids) {
            keys.add(KeyFactory.createKey("User", uid));
        }
        Map<Key, Entity> map = getDS().get(keys);
        List<User> users = new ArrayList<>();
        for (Key key : keys) {
            users.add(new User(map.get(key), User.Relation.STRANGER));
        }
        return users;
    }

    @ApiMethod(name = "samesiesApi.updateUser",
            path = "user/update",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateUser(User user) throws ServiceException {
        DatastoreService ds = getDS();
        if (user.getId() == null) {
            throw new BadRequestException("User ID not specified");
        }
        String newPassword = user.getNewPassword();
        User dsUser = getUserById(ds, user.getId(), User.Relation.SELF);
        if (dsUser == null) {
            throw new NotFoundException("User not found");
        } else if (newPassword != null) {
            // password is being changed
            if (BCrypt.checkpw(user.getPassword(), dsUser.getHashedPw())) {
                user.setPassword(user.getNewPassword());
            } else {
                throw new BadRequestException("Invalid Password");
            }
        }
        EntityUtils.put(ds, user);
    }

    @ApiMethod(name = "samesiesApi.findUser",
            path = "user/find/{email}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User findUser(@Named("email") String email) throws ServiceException {
        DatastoreService ds = getDS();

        return getUserByEmail(ds, email, User.Relation.STRANGER);
    }

    //----------------------------
    //        Friend Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getFriends",
            path = "friends/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Friend> getFriends(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Friend").setFilter(Query.CompositeFilterOperator.or(
                new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, uid),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, uid)));
        PreparedQuery pq = ds.prepare(query);
        List<Friend> friends = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Friend friend = new Friend(e);
            if (!friend.getStatus().isDeleted()) {
                long uid1 = friend.getUid1();
                long uid2 = friend.getUid2();
                User.Relation relation = friend.getStatus().getRelation();
                if (uid1 == uid) {
                    friend.setUser(getUserById(ds, uid2, relation));
                } else {
                    friend.setUser(getUserById(ds, uid1, relation));
                }
                friends.add(friend);
            }
        }
        return friends;
    }

    @ApiMethod(name = "samesiesApi.addFriend",
            path = "friends/add/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Friend addFriend(@Named("myId") long myId, @Named("theirId") long theirId) throws ServiceException {
        DatastoreService ds = getDS();

        if (myId == theirId) {
            throw new ForbiddenException("Cannot add oneself as a friend");
        }

        Friend friend;
        Query query = new Query("Friend").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, myId),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, theirId)));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            // Try the other order
            query = new Query("Friend").setFilter(Query.CompositeFilterOperator.and(
                    new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, theirId),
                    new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, myId)));
            pq = ds.prepare(query);
            e = pq.asSingleEntity();
            if (e == null) {
                // create new friend
                friend = new Friend(myId, theirId);
            } else {
                // friend exists in reverse order
                friend = new Friend(e);
                if (friend.getStatus() == Friend.Status.PENDING) {
                    // if both have added each other, accept
                    friend.setStatus(Friend.Status.ACCEPTED);
                } else if (friend.getStatus() == Friend.Status.DELETED_2) {
                    // if you had deleted them, set it back to pending
                    friend.setUid1(myId);
                    friend.setUid2(theirId);
                    friend.setStatus(Friend.Status.PENDING);
                }
                // if already accepted or deleted by them, leave it
            }
        } else {
            friend = new Friend(e);
            if (friend.getStatus() == Friend.Status.DELETED_1) {
                // if you had deleted them, set it back to pending
                friend.setStatus(Friend.Status.PENDING);
            }
            // if already pending, accepted, or deleted by them, leave it
        }
        if (friend.getStatus().isDeleted()) {
            return null;
        } else {
            EntityUtils.put(ds, friend); // update the friend in case it changed
            friend.setUser(getUserById(ds, theirId, friend.getStatus().getRelation()));
            return friend;
        }
    }

    @ApiMethod(name = "samesiesApi.removeFriend",
            path = "friends/remove/{id}/{myId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeFriend(@Named("id") long fid, @Named("myId") long myId) throws ServiceException {
        DatastoreService ds = getDS();
        try {
            Friend friend = new Friend(ds.get(KeyFactory.createKey("Friend", fid)));
            if (myId == friend.getUid1()) {
                friend.setStatus(Friend.Status.DELETED_1);
            } else {
                friend.setStatus(Friend.Status.DELETED_2);
            }
            EntityUtils.put(ds, friend);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Friend not found", e);
        }
    }

    //----------------------------
    //      Community Calls
    //----------------------------

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
            users.add(new User(e, User.Relation.STRANGER));
        }
        return new Community(location, users);
    }

    //----------------------------
    //       Question Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getQuestion",
            path = "question/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Question getQuestion(@Named("id") long qid) throws ServiceException {
        return getQuestion(getDS(), qid);
    }

    @ApiMethod(name = "samesiesApi.getQuestions",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getQuestions(@Named("ids") long[] qids) throws ServiceException {
        DatastoreService ds = getDS();
        List<Question> questions = new ArrayList<>();
        for (long qid : qids) {
            questions.add(getQuestion(ds, qid));
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.getAllQuestions",
            path = "questions/all",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getAllQuestions() throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Question")
                .addProjection(new PropertyProjection("q", String.class))
                .addProjection(new PropertyProjection("category", String.class));
        PreparedQuery pq = ds.prepare(query);
        List<Question> questions = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Question question = new Question(e);
            if (!question.getCategory().equals("suggestion")) {
                questions.add(question);
            }
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.getCategories",
            path = "questions/categories",
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

    @ApiMethod(name = "samesiesApi.suggestQuestion",
            path = "questions/suggest/{question}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void suggestQuestion(@Named("question") String question) throws ServiceException {
        Question q = new Question(question, null, "suggestion");
        EntityUtils.put(getDS(), q);
    }

    //----------------------------
    //       Episode Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getModes",
            path = "episodes/modes",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<String> getModes() throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Mode").setKeysOnly();
        PreparedQuery pq = ds.prepare(query);
        List<String> modes = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            modes.add(e.getKey().getName());
        }
        return modes;
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
            path = "episode/find/{myId}/{mode}/{matchMale}/{matchFemale}/{matchOther}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode findEpisode(@Named("myId") long myUid, @Named("mode") String mode, @Named("matchMale") boolean matchMale,
                               @Named("matchFemale") boolean matchFemale, @Named("matchOther") boolean matchOther) throws ServiceException {
        DatastoreService ds = getDS();

        Settings settings = new Settings(mode, matchMale, matchFemale, matchOther);

        Query query = new Query("Episode").setFilter(Query.CompositeFilterOperator.and(
                        new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, Episode.Status.MATCHING.name()),
                        new Query.FilterPredicate("isPersistent", Query.FilterOperator.EQUAL, false),
                        new Query.FilterPredicate("mode", Query.FilterOperator.EQUAL, mode)))
                .addSort("startDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);

        Iterator<Entity> iter = pq.asIterator();
        Episode episode = null;
        while (episode == null && iter.hasNext()) {
            Episode temp = new Episode(iter.next());
            if (isMatch(ds, myUid, settings, temp.getUid1(), temp.getSettings())) {
                episode = temp;
            }
        }
        if (episode == null) {
            episode = new Episode(myUid, settings);
        } else {
            episode.setStatus(Episode.Status.IN_PROGRESS);
            episode.setUid2(myUid);
            episode.setQids(getQids(ds, mode));
        }
        EntityUtils.put(ds, episode);
        return episode;
    }

    @ApiMethod(name = "samesiesApi.connectEpisode",
            path = "episode/connect/{myId}/{theirId}/{mode}/{matchMale}/{matchFemale}/{matchOther}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode connectEpisode(@Named("myId") long myUid, @Named("theirId") long theirUid, @Named("mode") String mode,
                                  @Named("matchMale") boolean matchMale, @Named("matchFemale") boolean matchFemale,
                                  @Named("matchOther") boolean matchOther) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = new Episode(myUid, theirUid, new Settings(mode, matchMale, matchFemale, matchOther));
        episode.setQids(getQids(ds, mode));
        EntityUtils.put(ds, episode);
        return episode;
    }

    @ApiMethod(name = "samesiesApi.acceptEpisode",
            path = "episode/accept/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void acceptEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        episode.setStatus(Episode.Status.IN_PROGRESS);
        EntityUtils.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.getEpisode",
            path = "episode/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Episode getEpisode(@Named("id") long eid) throws ServiceException {
        return getEpisode(getDS(), eid);
    }

    @ApiMethod(name = "samesiesApi.answerEpisode",
            path = "episode/answer/{id}/{myId}/{answer}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Episode answerEpisode(@Named("id") long eid, @Named("myId") long myUid,
                                 @Named("answer") String answer) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        boolean is1 = (myUid == episode.getUid1());
        List<String> answers = episode.getAnswers(is1);
        if (answers == null) {
            answers = new ArrayList<>();
        }
        answers.add(answer);
        episode.setAnswers(is1, answers);
        EntityUtils.put(ds, episode);
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
        EntityUtils.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.getConnections",
            path = "connections/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Episode> getConnections(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Episode").setFilter(Query.CompositeFilterOperator.and(
                Query.CompositeFilterOperator.or(
                        new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, uid),
                        new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, uid)),
                new Query.FilterPredicate("isPersistent", Query.FilterOperator.EQUAL, true)))
                .addSort("startDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);

        List<Episode> connections = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Episode episode = new Episode(e);
            if (episode.getStatus() == Episode.Status.MATCHING || episode.getStatus() == Episode.Status.IN_PROGRESS) {
                connections.add(new Episode(e));
            }
        }
        return connections;
    }

    @ApiMethod(name = "samesiesApi.getEpisodeQuestions",
            path = "episodes/questions/{eid}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getEpisodeQuestions(@Named("eid") long eid) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        List<Question> questions = new ArrayList<>();
        if (episode.getSettings().getMode().equals("Personal")) {
            User u1 = getUserById(ds, episode.getUid1(), User.Relation.ADMIN);
            User u2 = getUserById(ds, episode.getUid2(), User.Relation.ADMIN);
            for (int i=0; i<5; i++) {
                questions.add(new Question(u1.getQuestions().get(i)));
                questions.add(new Question(u2.getQuestions().get(i)));
            }
        } else {
            for (long qid : episode.getQids()) {
                questions.add(getQuestion(ds, qid));
            }
        }
        return questions;
    }

    //----------------------------
    //        Chat Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.startChat",
            path = "chat/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Chat startChat(@Named("myId") long myUid, @Named("theirId") long theirUid) throws ServiceException {
        DatastoreService ds = getDS();
        long[] uids = orderIds(myUid, theirUid); // avoid parity issues to simplify search
        Chat chat = getChat(ds, uids); // make sure a chat between these users doesn't already exist
        if (chat == null) {
            chat = new Chat(uids[0], uids[1]);
            EntityUtils.put(ds, chat);
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
    public Message sendMessage(@Named("chatId") long cid, @Named("myId") long myUid, @Named("message") String message,
                               @Named("random") @Nullable String random) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, cid);
        Message m = new Message(cid, myUid, message, random);
        chat.setLastModified(m.getSentDate());
        EntityUtils.put(ds, chat, m);
        return m;
    }

    @ApiMethod(name = "samesiesApi.getMessages",
            path = "message/{chatId}/{after}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Message> getMessages(@Named("chatId") long cid, @Named("after") Date after) throws ServiceException {
        DatastoreService ds = getDS();
        Query query = new Query("Message").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("chatId", Query.FilterOperator.EQUAL, cid),
                new Query.FilterPredicate("sentDate", Query.FilterOperator.GREATER_THAN, after)))
                .addSort("sentDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);
        List<Message> messages = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            messages.add(new Message(e));
        }
        return messages;
    }

    //----------------------------
    //      Feedback Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.sendFeedback",
            path = "feedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendFeedback(Feedback feedback) throws ServiceException {
        EntityUtils.put(getDS(), feedback);
    }

    //----------------------------
    //   Static Helper Methods
    //----------------------------

    private static DatastoreService getDS() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * This method decides whether a user is compatible with a matching-state episode
     * @param ds
     * @param uid1
     * @param settings1
     * @param uid2
     * @param settings2
     * @return
     */
    private static boolean isMatch(DatastoreService ds, Long uid1, Settings settings1, Long uid2, Settings settings2) throws NotFoundException {
        if (uid1.equals(uid2)) { // cannot match with yourself (somehow)
            return false;
        }
        // finally check that genders are acceptable to each other
        return isMatch(getUserById(ds, uid1, User.Relation.ADMIN).getGender(), settings2)
                && isMatch(getUserById(ds, uid2, User.Relation.ADMIN).getGender(), settings1);
    }

    private static boolean isMatch(String gender, Settings settings) {
        if (gender == null) {
            return settings.getMatchOther();
        }
        switch (gender) {
            case "Male":
                return settings.getMatchMale();
            case "Female":
                return settings.getMatchFemale();
            default:
                return settings.getMatchOther();
        }
    }

    private static User getUserById(DatastoreService ds, long id, User.Relation relation) throws NotFoundException {
        try {
            return new User(ds.get(KeyFactory.createKey("User", id)), relation);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Email not found", e);
        }
    }

    private static User getUserByEmail(DatastoreService ds, String email, User.Relation relation) {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = ds.prepare(query);

        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new User(e, relation);
        }
    }

    private static Question getQuestion(DatastoreService ds, long qid) throws NotFoundException {
        try {
            return new Question(ds.get(KeyFactory.createKey("Question", qid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Question not found", e);
        }
    }

    private static List<Long> getQids(DatastoreService ds, String mode) {
        List<Long> questions = new ArrayList<>();
        if (!mode.equals("Personal")) { // personal questions can be ignored at this stage
            Query query = new Query("Question").setKeysOnly();
            // TODO: when we have more categories, this code might get more complex
            query.setFilter(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, mode));
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
            for (int i=0; i<10; i++) {
                questions.add(keys.get(a[i]).getKey().getId());
            }
        }
        return questions;
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
        Query query = new Query("Chat").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, uids[0]),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, uids[1])));
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
