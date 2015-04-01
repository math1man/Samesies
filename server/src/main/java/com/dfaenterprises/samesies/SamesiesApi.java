package com.dfaenterprises.samesies;

import apns.*;
import apns.keystore.ClassPathResourceKeyStoreProvider;
import apns.keystore.KeyStoreProvider;
import apns.keystore.KeyStoreType;
import com.dfaenterprises.samesies.model.*;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;

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

    // TODO: somehow hide this password?
    public static final char[] APN_CERTIFICATE_PASSWORD = null;
    public static final String GCM_API_KEY = null;
    public static final long EVERYONE_CID = 5686812383117312L;

    public void initQuestions() throws ServiceException {
        DatastoreService ds = getDS();

        Question[] questions = {
                new Question("What do you like to do on a first date?"),
                new Question("What is your go-to conversation starter?"),
                new Question("If you had to take a date to dinner, where would you go?"),
                new Question("If you could have a superpower, what would it be and why?"),
                new Question("Given the choice of anyone in the world, whom would you want as a dinner guest?"),
                new Question("Would you like to be famous? In what way?"),
                new Question("Before making a telephone call, do you ever rehearse what you are going to say and why?"),
                new Question("What would constitute a \"perfect\" day for you?"),
                new Question("When did you last sing to yourself? To someone else?"),
                new Question("If you were able to live to the age of 90 and retain either the mind or body of a " +
                        "30-year-old for the last 60 years of your life, which would you want and why?"),
                new Question("For what in your life do you feel most grateful?"),
                new Question("If you could change anything about the way you were raised, what would it be?"),
                new Question("If you could wake up tomorrow having gained any one quality or ability, what would it be?"),
                new Question("If a crystal ball could tell you the truth about yourself, your life, the future " +
                        "or any one thing, what would you want to know?"),
                new Question("Is there something that you've dreamed of doing for a long time? Why haven't you done it?"),
                new Question("What is the greatest accomplishment of your life?"),
                new Question("What do you value most in a friendship?"),
                new Question("What is your most treasured memory?"),
                new Question("What is your most terrible memory?"),
                new Question("If you knew that in one year you would die suddenly, would you change anything " +
                        "about the way you are now living and why?"),
                new Question("When did you last cry in front of another person? By yourself?"),
                new Question("What, if anything, is too serious to be joked about?"),
                new Question("If you were to die this evening with no opportunity to communicate with anyone, " +
                        "what would you most regret not having told someone? Why haven't you told them yet?"),
                new Question("Your house, containing everything you own, catches fire. After saving your loved ones and " +
                        "pets, you have time to safely make a final dash to save any one item. What would it be and why?"),
                new Question("What is the most important thing your close friends should know about you?"),
                new Question("What is one of your most embarrassing moments?")
        };
        for (Question q : questions) {
            q.setCategory("Random");
        }
        EntityUtils.put(ds, questions);

        ds.put(Arrays.asList(new Entity("Category", "All"),
                new Entity("Category", "Random")));
    }

    public void initModes() throws ServiceException {
        DatastoreService ds = getDS();
        EntityUtils.put(ds, new Mode("Random", "Answer 10 random questions from our database."),
                new Mode("Personal", "Answer each of your and your partner's 5 personal questions."));
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
        User dsUser = getUser(ds, email, User.Relation.SELF, false);
        if (dsUser.getIsBanned()) {
            throw new ForbiddenException("Account has been banned");
        } else if (!dsUser.getIsActivated()) {
            throw new ForbiddenException("Account has not been activated");
        } else if (password != null && BCrypt.checkpw(password, dsUser.getHashedPw())) {
            return dsUser;
        } else {
            throw new BadRequestException("Incorrect Password");
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
        if (getUser(ds, email, User.Relation.STRANGER, true) == null) {
            newUser.initNewUser();
            EntityUtils.put(ds, newUser);
            EntityUtils.put(ds, new CommunityUser(EVERYONE_CID, newUser.getId(), true));
            sendEmail(email, "Activate your Samesies Account",
                    "Click the link below to activate your account:\n" +
                            "https://samesies-app.appspot.com/_ah/spi/activate?user_id=" + newUser.getId() + "\n\n" +
                            "Have fun,\n" +
                            "The Samesies Team");
            return newUser;
        } else {
            throw new ForbiddenException("Email already in use");
        }
    }

    @ApiMethod(name = "samesiesApi.recoverUser",
            path = "user/recover/{email}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void recoverUser(@Named("email") String email) throws ServiceException {
        DatastoreService ds = getDS();
        User user = getUser(ds, email, User.Relation.ADMIN, false);
        String tempPass = EntityUtils.randomString(8);
        user.setPassword(tempPass);
        EntityUtils.put(ds, user);
        sendEmail(user, "Samesies Password Reset",
                "Your password has been reset.  Your new temporary password is " + tempPass + ".  " +
                        "We recommend you change this password immediately once you log in to Samesies.\n\n" +
                        "All the best,\n" +
                        "The Samesies Team");
    }

    @ApiMethod(name = "samesiesApi.getUser",
            path = "user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User getUser(@Named("id") long uid) throws ServiceException {
        return getUser(getDS(), uid, User.Relation.STRANGER, false);
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
        User dsUser = getUser(ds, user.getId(), User.Relation.SELF, false);
        if (newPassword != null) {
            // password is being changed
            if (BCrypt.checkpw(user.getPassword(), dsUser.getHashedPw())) {
                user.setPassword(newPassword);
            } else {
                throw new BadRequestException("Invalid Password");
            }
        }
        user.setGeoPt(dsUser.getGeoPt());
        user.setIsActivated(dsUser.getIsActivated());
        user.setIsBanned(dsUser.getIsBanned());
        EntityUtils.put(ds, user);
    }

    @ApiMethod(name = "samesiesApi.findUser",
            path = "user/find/{email}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User findUser(@Named("email") String email) throws ServiceException {
        User user = getUser(getDS(), email, User.Relation.STRANGER, true);
        if (isValid(user)) {
            return user;
        } else {
            return null;
        }
    }

    @ApiMethod(name = "samesiesApi.searchUsers",
            path = "user/search/{string}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<User> searchUsers(@Named("string") String string) throws ServiceException {
        DatastoreService ds = getDS();
        // first, lets check if they straight entered an email
        User user = getUser(getDS(), string, User.Relation.STRANGER, true);
        if (isValid(user)) { // ignore banned users
            return Collections.singletonList(user);
        } else {
            Query query = new Query("User");
            PreparedQuery pq = ds.prepare(query);
            List<User> users = new ArrayList<>();
            Pattern pattern = getSearchPattern(string);
            for (Entity e : pq.asIterable()) {
                User u = new User(e);
                if (!u.getIsBanned() && (u.getName() != null && pattern.matcher(u.getName().toLowerCase()).matches()
                        || u.getAlias() != null && pattern.matcher(u.getAlias().toLowerCase()).matches())) {
                    users.add(new User(e, User.Relation.STRANGER));
                    if (users.size() == 10) {
                        return users;
                    }
                }
            }
            return users;
        }
    }

    //----------------------------
    //  Disciplinary User Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.flagUser",
            path = "user/flag/{flaggedId}/{flaggerId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void flagUser(@Named("flaggedId") long flaggedId, @Named("flaggerId") long flaggerId,
                     @Nullable @Named("reason") String reason) throws ServiceException {
        DatastoreService ds = getDS();
        Flag flag = new Flag(flaggedId, flaggerId, reason);
        EntityUtils.put(ds, flag);
    }

    @ApiMethod(name = "samesiesApi.banUser",
            path = "user/ban/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void banUser(@Named("id") long uid, @Nullable @Named("isBanned") Boolean isBanned) throws ServiceException {
        DatastoreService ds = getDS();
        if (isBanned == null) {
            isBanned = true;
        }
        User user = getUser(ds, uid, User.Relation.ADMIN, false);
        user.setIsBanned(isBanned);
        EntityUtils.put(ds, user);
        sendEmail(user, "Samesies Account Banned",
                "We are sorry to inform you that your account has been banned from Samesies.  " +
                        "We received multiple complaints that your profile picture was inappropriate, " +
                        "so we have taken action.\n\n" +
                        "The Samesies Team");
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
                long theirUid = friend.getOtherUid(uid);
                User.Relation relation = friend.getStatus().getRelation();
                User user = getUser(ds, theirUid, relation, true);
                if (isValid(user)) {
                    friend.setUser(user);
                    friends.add(friend);
                }
            }
        }
        return friends;
    }

    @ApiMethod(name = "samesiesApi.checkFriend",
            path = "friends/check/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Friend checkFriend(@Named("myId") long myId, @Named("theirId") long theirId) throws ServiceException {
        return getFriend(getDS(), myId, theirId);
    }

    @ApiMethod(name = "samesiesApi.addFriend",
            path = "friends/add/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Friend addFriend(@Named("myId") long myId, @Named("theirId") long theirId) throws ServiceException {
        DatastoreService ds = getDS();

        if (myId == theirId) {
            throw new ForbiddenException("Cannot add oneself as a friend");
        }

        Friend friend = getFriend(ds, myId, theirId);
        if (friend == null) {
            friend = new Friend(myId, theirId);
        } else if (friend.isUid1(myId)) {
            if (friend.getStatus() == Friend.Status.DELETED_1) {
                // if you had deleted them, set it back to pending
                friend.setStatus(Friend.Status.PENDING);
            }
            // if already pending, accepted, or deleted by them, leave it
        } else {
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
        if (friend.getStatus().isDeleted()) {
            return null;
        } else {
            EntityUtils.put(ds, friend); // update the friend in case it changed
            friend.setUser(getUser(ds, theirId, friend.getStatus().getRelation(), true));
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
            if (friend.isUid1(myId)) {
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
            path = "community",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Community getCommunity(@Nullable@Named("location") String location, @Nullable@Named("name") String name, @Nullable@Named("id") Long cid) throws ServiceException {
        // **v1.1.0** TODO: eventually remove name and make cid not Nullable, but needed for compatibility
        DatastoreService ds = getDS();
        List<User> users = new ArrayList<>();
        if (cid == null) {
            // **v1.0.0** TODO: eventually remove location, but needed for compatibility
            if (name == null) {
                if (location == null) {
                    throw new BadRequestException("Must specify a community");
                } else {
                    name = location;
                }
            }
            Query query = new Query("User").setFilter(Query.CompositeFilterOperator.and(
                    new Query.FilterPredicate("community", Query.FilterOperator.EQUAL, name),
                    new Query.FilterPredicate("isBanned", Query.FilterOperator.EQUAL, false)));
            PreparedQuery pq = ds.prepare(query);
            for (Entity e : pq.asIterable()) {
                users.add(new User(e, User.Relation.STRANGER));
            }
        } else {
            name = getCommunity(ds, cid).getName();
            Query query = new Query("CommunityUser").setFilter(Query.CompositeFilterOperator.and(
                    new Query.FilterPredicate("cid", Query.FilterOperator.EQUAL, cid),
                    new Query.FilterPredicate("isValidated", Query.FilterOperator.EQUAL, true)));
            PreparedQuery pq = ds.prepare(query);
            for (Entity e : pq.asIterable()) {
                User user = getUser(ds, new CommunityUser(e).getUid(), User.Relation.STRANGER, true);
                if (isValid(user)) {
                    users.add(user);
                }
            }
        }
        Collections.shuffle(users);
        if (users.size() > 100) {
            // only return 100 users max
            // need this out here for randomization reasons
            return new Community(name, users.subList(0, 100));
        } else {
            return new Community(name, users);
        }
    }

    @ApiMethod(name = "samesiesApi.getNearBy",
            path = "nearby/{latitude}/{longitude}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Community getNearBy(@Named("latitude") float latitude, @Named("longitude") float longitude) throws ServiceException {
        DatastoreService ds = getDS();
        GeoPt location = new GeoPt(latitude, longitude);
        Query query = new Query("User").setFilter(new Query.FilterPredicate("isBanned", Query.FilterOperator.EQUAL, false));
        PreparedQuery pq = ds.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            User user = new User(e, User.Relation.STRANGER);
            if (user.hasGeoPt() && distance(user.getGeoPt(), location) <= 10) {
                users.add(user);
            }
        }
        Collections.shuffle(users);
        return new Community("Near By", users);
    }

    @ApiMethod(name = "samesiesApi.getUserCommunities",
            path = "communities/user/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Community> getUserCommunities(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = getDS();
        Query query = new Query("CommunityUser").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("uid", Query.FilterOperator.EQUAL, uid),
                new Query.FilterPredicate("isValidated", Query.FilterOperator.EQUAL, true)));
        PreparedQuery pq = ds.prepare(query);
        List<Community> communities = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Community community = getCommunity(ds, new CommunityUser(e).getCid());
            if (community != null) {
                communities.add(community);
            }
        }
        return communities;
    }

    @ApiMethod(name = "samesiesApi.searchCommunities",
            path = "communities/search/{string}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Community> searchCommunities(@Named("string") String string) throws ServiceException {
        DatastoreService ds = getDS();
        Query query = new Query("Community").setFilter(new Query.FilterPredicate(
                "state", Query.FilterOperator.EQUAL, Community.State.ACTIVE.name()));
        PreparedQuery pq = ds.prepare(query);
        List<Community> communities = new ArrayList<>();
        Pattern pattern = getSearchPattern(string);
        for (Entity e : pq.asIterable()) {
            Community c = new Community(e);
            if (c.getName() != null && pattern.matcher(c.getName().toLowerCase()).matches()) {
                communities.add(c);
                if (communities.size() == 10) {
                    return communities;
                }
            }
        }
        return communities;
    }

    @ApiMethod(name = "samesiesApi.joinCommunity",
            path = "communities/join/{id}/{myId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Community joinCommunity(@Named("id") long cid, @Named("myId") long myUid, @Nullable@Named("string") String string) throws ServiceException {
        DatastoreService ds = getDS();
        Community community = getCommunity(ds, cid);
        CommunityUser cu = getCommunityUser(ds, cid, myUid);
        if (cu == null) {
            // CommunityUser does not exist, make new one
            switch (community.getType()) {
                case OPEN:
                    EntityUtils.put(ds, new CommunityUser(community.getId(), myUid, true));
                    return community;
                case EMAIL:
                    // utility string is an email domain
                    if (string != null && string.contains(community.getUtilityString())) {
                        cu = new CommunityUser(community.getId(), myUid);
                        EntityUtils.put(ds, cu);
                        sendEmail(string, "Join " + community.getName(),
                                "Click the link below to join the Samesies community for " + community.getName() + ":\n" +
                                "https://samesies-app.appspot.com/_ah/spi/communities/join?community_user_id=" + cu.getId() + "\n\n" +
                                "Have fun,\n" +
                                "The Samesies Team");
                    }
                    break;
                case PASSWORD:
                    // utility string is a password
                    if (string != null && BCrypt.checkpw(string, community.getUtilityString())) {
                        EntityUtils.put(ds, new CommunityUser(community.getId(), myUid, true));
                        return community;
                    }
                    break;
                case EXCLUSIVE:
                    EntityUtils.put(ds, new CommunityUser(community.getId(), myUid));
                    break;
            }
        } else {
            // CommunityUser already exists, return community if validated
            if (cu.getIsValidated()) {
                return community;
            }
        }
        return null;
    }

    @ApiMethod(name = "samesiesApi.createOpenCommunity",
            path = "communities/create/{name}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createOpenCommunity(@Named("name") String name, @Nullable@Named("description") String description) throws ServiceException {
        EntityUtils.put(getDS(), new Community(name, description));
    }

    @ApiMethod(name = "samesiesApi.createEmailCommunity",
            path = "communities/create/email/{name}/{emailSuffix}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createEmailCommunity(@Named("name") String name, @Named("emailSuffix") String emailSuffix,
                                     @Nullable@Named("description") String description) throws ServiceException {
        EntityUtils.put(getDS(), new Community(name, description, Community.Type.EMAIL, emailSuffix));
    }

    @ApiMethod(name = "samesiesApi.createPasswordCommunity",
            path = "communities/create/password/{name}/{password}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createPasswordCommunity(@Named("name") String name, @Named("password") String password,
                                        @Nullable@Named("description") String description) throws ServiceException {
        EntityUtils.put(getDS(), new Community(name, description, Community.Type.PASSWORD,
                BCrypt.hashpw(password, BCrypt.gensalt())));
    }

    @ApiMethod(name = "samesiesApi.createExclusiveCommunity",
            path = "communities/create/exclusive/{name}/{password}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createExclusiveCommunity(@Named("name") String name, @Named("password") String adminPassword,
                                        @Nullable@Named("description") String description) throws ServiceException {
        EntityUtils.put(getDS(), new Community(name, description, Community.Type.EXCLUSIVE,
                BCrypt.hashpw(adminPassword, BCrypt.gensalt())));
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
        Question q = new Question(question, "Suggestion");
        EntityUtils.put(getDS(), q);
    }

    //----------------------------
    //       Episode Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getModes",
            path = "episodes/modes",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Mode> getModes() throws ServiceException {
        DatastoreService ds = getDS();

        Query query = new Query("Mode");
        PreparedQuery pq = ds.prepare(query);
        List<Mode> modes = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            modes.add(new Mode(e));
        }
        return modes;
    }

    @ApiMethod(name = "samesiesApi.findEpisode",
            path = "episode/find/{myId}/{mode}/{matchMale}/{matchFemale}/{matchOther}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode findEpisode(@Named("myId") long myUid, @Named("mode") String mode, @Named("matchMale") boolean matchMale,
                               @Named("matchFemale") boolean matchFemale, @Named("matchOther") boolean matchOther,
                               @Nullable@Named("isPersistent") Boolean isPersistent, @Nullable@Named("cid") Long cid,
                               @Nullable@Named("latitude") Float latitude, @Nullable@Named("longitude") Float longitude) throws ServiceException {
        DatastoreService ds = getDS();
        // **v1.1.0** TODO: make isPersistent not Nullable
        if (isPersistent == null) {
            isPersistent = false;
        }
        GeoPt location = null;
        if (latitude != null && longitude != null) {
            location = new GeoPt(latitude, longitude);
        }
        Settings settings = new Settings(mode, matchMale, matchFemale, matchOther, location, cid);

        Query query = new Query("Episode").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, Episode.Status.MATCHING.name()),
                new Query.FilterPredicate("isPersistent", Query.FilterOperator.EQUAL, isPersistent),
                new Query.FilterPredicate("mode", Query.FilterOperator.EQUAL, mode)))
                .addSort("startDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);

        Iterator<Entity> iter = pq.asIterator();
        Episode episode = null;
        while (episode == null && iter.hasNext()) {
            Episode temp = new Episode(iter.next());
            if (temp.getUid2() == null) {
                if (isPersistent && temp.getUid1() == myUid) {
                    return temp; // only 1 persistent random match per person per mode
                }
                // check that the episode was last modified less than a minutes ago
                if (isPersistent || new Date().getTime() - temp.getLastModified().getTime() < 1000 * 60) {
                    if (isMatch(ds, myUid, settings, temp.getUid1(), temp.getSettings())) {
                        episode = temp;
                    }
                } else {
                    // if it hasn't been modified in over a minute, the person is not there
                    temp.setStatus(Episode.Status.UNMATCHED);
                    EntityUtils.put(ds, temp);
                }
            }
        }
        if (episode == null) {
            episode = new Episode(myUid, isPersistent, settings);
        } else {
            episode.setStatus(Episode.Status.IN_PROGRESS);
            episode.setUid2(myUid);
            episode.setQids(getQids(ds, mode));
            episode.setUser(getUser(ds, episode.getUid1(), User.Relation.STRANGER, true));
            episode.modify();
        }
        EntityUtils.put(ds, episode);
        return episode;
    }

    @ApiMethod(name = "samesiesApi.connectEpisode",
            path = "episode/connect/{myId}/{theirId}/{mode}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode connectEpisode(@Named("myId") long myUid, @Named("theirId") long theirUid, @Named("mode") String mode) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = new Episode(myUid, theirUid, new Settings(mode));
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
        episode.modify();
        EntityUtils.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.getEpisode",
            path = "episode/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Episode getEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        if (!episode.getIsPersistent() && episode.getStatus() == Episode.Status.MATCHING) {
            // update it for matching purposes so that the matching system can discard old episodes
            episode.setLastModified(new Date());
            EntityUtils.put(ds, episode);
        }
        return episode;
    }

    @ApiMethod(name = "samesiesApi.answerEpisode",
            path = "episode/answer/{id}/{myId}/{answer}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Episode answerEpisode(@Named("id") long eid, @Named("myId") long myUid,
                                 @Named("answer") String answer) throws ServiceException {
        DatastoreService ds = getDS();
        Episode episode = getEpisode(ds, eid);
        List<String> answers = episode.getAnswers(myUid);
        if (answers == null) {
            answers = new ArrayList<>();
        }
        // Add to the answer database
        Answer a = null;
        if (!episode.isPersonal()) {
            int index = answers.size();
            a = new Answer(episode.getQids().get(index), myUid, answer);
        }
        answers.add(answer);
        episode.setAnswers(myUid, answers);
        episode.modify();
        EntityUtils.put(ds, episode, a);
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
        episode.modify();
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
                Long otherUid = episode.getOtherUid(uid);
                if (otherUid == null) {
                    connections.add(episode);
                } else {
                    User user = getUser(ds, episode.getOtherUid(uid), User.Relation.STRANGER, true);
                    if (isValid(user)) {
                        episode.setUser(user);
                        connections.add(episode);
                    }
                }
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
        if (episode.isPersonal()) {
            User u1 = getUser(ds, episode.getUid1(), User.Relation.ADMIN, false);
            User u2 = getUser(ds, episode.getUid2(), User.Relation.ADMIN, false);
            try {
                for (int i=0; i<5; i++) {
                    questions.add(new Question(u1.getQuestions().get(i)));
                    questions.add(new Question(u2.getQuestions().get(i)));
                }
            } catch (IndexOutOfBoundsException e) {
                throw new NotFoundException("One or more of the users is missing questions");
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
            path = "chat/{eofid}/{isEpisode}/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Chat startChat(@Named("eofid") long eofid, @Named("isEpisode") boolean isEpisode,
                          @Named("myId") long myUid, @Named("theirId") long theirUid) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, eofid, isEpisode);
        if (chat == null) {
            chat = new Chat(eofid, isEpisode, myUid, theirUid);
        } else {
            chat.setIsClosed(false);
            chat.setIsUpToDate(myUid, true);
        }
        EntityUtils.put(ds, chat);
        return chat;
    }

    @ApiMethod(name = "samesiesApi.getChat",
            path = "chat/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Chat getChat(@Named("id") long cid) throws ServiceException {
        return getChat(getDS(), cid);
    }

    @ApiMethod(name = "samesiesApi.getChats",
            path = "chats/{myId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Chat> getChats(@Named("myId") long myUid) throws ServiceException {
        DatastoreService ds = getDS();
        Query query = new Query("Chat").setFilter(Query.CompositeFilterOperator.and(
                Query.CompositeFilterOperator.or(
                        new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, myUid),
                        new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, myUid)),
                new Query.FilterPredicate("isClosed", Query.FilterOperator.EQUAL, false)));
        PreparedQuery pq = ds.prepare(query);
        List<Chat> chats = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Chat chat = new Chat(e);
            User user = getUser(ds, chat.getOtherUid(myUid), User.Relation.STRANGER, true);
            if (isValid(user)) {
                chat.setUser(user);
                chats.add(chat);
            }
        }
        return chats;
    }

    @ApiMethod(name = "samesiesApi.updateChat",
            path = "chat/update/{id}/{eofid}/{isEpisode}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateChat(@Named("id") long cid, @Named("eofid") long eofid, @Named("isEpisode") boolean isEpisode) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, cid);
        chat.setEofid(eofid);
        chat.setIsEpisode(isEpisode);
        EntityUtils.put(ds, chat);
    }

    @ApiMethod(name = "samesiesApi.closeChat",
            path = "chat/close/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void closeChat(@Named("id") long cid) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, cid);
        chat.setIsClosed(true);
        EntityUtils.put(ds, chat);
    }

    @ApiMethod(name = "samesiesApi.sendMessage",
            path = "chat/message/{chatId}/{myId}/{message}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Message sendMessage(@Named("chatId") long cid, @Named("myId") long myUid, @Named("message") String message,
                               @Named("random") @Nullable String random) throws ServiceException {
        DatastoreService ds = getDS();
        Chat chat = getChat(ds, cid);
        Message m = new Message(cid, myUid, message, random);
        chat.update(myUid, m.getSentDate());
        EntityUtils.put(ds, chat, m);
        return m;
    }

    @ApiMethod(name = "samesiesApi.getMessages",
            path = "chat/messages/{chatId}/{after}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Message> getMessages(@Named("chatId") long cid, @Named("after") Date after, @Nullable@Named("myId") Long myUid) throws ServiceException {
        // **v1.0.0** TODO: eventually remove the @Nullable to the myUid parameter
        // For now we need it for backwards compatibility
        DatastoreService ds = getDS();
        if (myUid != null) {
            Chat chat = getChat(ds, cid);
            chat.setIsUpToDate(myUid, true);
            EntityUtils.put(ds, chat);
        }
        Query query = new Query("Message").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("chatId", Query.FilterOperator.EQUAL, cid),
                new Query.FilterPredicate("sentDate", Query.FilterOperator.GREATER_THAN, after)))
                .addSort("sentDate", Query.SortDirection.ASCENDING);
        PreparedQuery pq = ds.prepare(query);
        List<Message> messages = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            messages.add(new Message(e));
        }
        int size = messages.size();
        if (size > 100) {
            messages = messages.subList(size - 100, size);
        }
        return messages;
    }

    //----------------------------
    //    Miscellaneous Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.sendFeedback",
            path = "feedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendFeedback(Feedback feedback) throws ServiceException {
        EntityUtils.put(getDS(), feedback);
    }

    @ApiMethod(name = "samesiesApi.sendEmail",
            path = "email/{uid}/{subject}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendEmail(@Named("uid") long uid, @Named("subject") String subject, @Named("message") String[] messageLines) throws ServiceException {
        StringBuilder sb = new StringBuilder();
        for (String s : messageLines) {
            sb.append(s).append('\n');
        }
        sendEmail(getUser(getDS(), uid, User.Relation.ADMIN, false), subject, sb.toString());
    }

    @ApiMethod(name = "samesiesApi.registerPush",
            path = "push/register/{id}/{type}/{deviceToken}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void registerPush(@Named("id") long uid, @Named("type") String type, @Named("deviceToken") String deviceToken) throws ServiceException {
        DatastoreService ds = getDS();
        type = type.toLowerCase();
        Push push = getPush(ds, type, deviceToken);
        if (push == null) {
            push = new Push(uid, type.toLowerCase(), deviceToken);
        } else {
            push.setUid(uid);
        }
        EntityUtils.put(getDS(), push);
    }

    @ApiMethod(name = "samesiesApi.sendPush",
            path = "push/send/{id}/{title}/{message}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendPush(@Named("id") long pid, @Named("title") String title, @Named("message") String message) throws ServiceException {
        DatastoreService ds = getDS();
        Push push = getPush(ds, pid);
        sendPush(push, title, message);
    }

    //----------------------------
    //   Static Helper Methods
    //----------------------------

    private static DatastoreService getDS() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * This method decides whether a two user-settings pairs are compatible.
     * First, it double-checks to make sure the uids are different or returns false.
     * Second, it checks if the community/location settings match. If both have communities that are
     * different, it returns false. If either did not have a community, it checks if they both have
     * locations. If the locations are more than 10 miles apart, it returns false.
     * Third, it checks if their last match was each other. If true, it returns false.
     * Finally, it returns whether or not their genders/preferences are compatible.
     * @param ds
     * @param uid1
     * @param settings1
     * @param uid2
     * @param settings2
     * @return
     */
    private static boolean isMatch(DatastoreService ds, long uid1, Settings settings1, long uid2, Settings settings2) throws NotFoundException {
        // cannot match with yourself (somehow)
        if (uid1 == uid2) {
            return false;
        }
        // check community
        if (settings1.hasCommunity() && settings2.hasCommunity()) {
            // communities must be the same
            if (!settings1.getCid().equals(settings2.getCid())) {
                return false;
            }
        } else if (settings1.hasLocation() && settings2.hasLocation()) {
            // if no communities, check location
            // location distance must be under 10 miles
            if (distance(settings1.getLocation(), settings2.getLocation()) > 10) {
                return false;
            }
        } else if (settings1.hasCommunity() || settings1.hasLocation()
                || settings2.hasCommunity() || settings2.hasLocation()) {
            // match no parameters with no parameters
            return false;
        }
        // make sure their last pairing wasn't each other
        // confirm the episode was a match by searching uid2 (if not matched, would be null)
        if (isLastMatched(ds, uid1, uid2) || isLastMatched(ds, uid2, uid1)) {
            return false;
        }

        // finally check that genders are acceptable to each other
        return checkGender(getUser(ds, uid1, User.Relation.ADMIN, false).getGender(), settings2)
                && checkGender(getUser(ds, uid2, User.Relation.ADMIN, false).getGender(), settings1);
    }

    private static boolean checkGender(String gender, Settings settings) {
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

    /**
     * http://en.wikipedia.org/wiki/Haversine_formula
     * @param location1
     * @param location2
     * @return
     */
    private static double distance(GeoPt location1, GeoPt location2) {
        double r = 3959; // miles (6371 km)
        double lat1 = Math.toRadians(location1.getLatitude());
        double lon1 = Math.toRadians(location1.getLongitude());
        double lat2 = Math.toRadians(location2.getLatitude());
        double lon2 = Math.toRadians(location2.getLongitude());
        double h = haversin(lat2 - lat1) + Math.cos(lat1) * Math.cos(lat2) * haversin(lon2 - lon1);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    private static double haversin(double radians) {
        return Math.sin(radians / 2) * Math.sin(radians / 2);
    }

    private static Pattern getSearchPattern(String string) {
        return Pattern.compile(".*" + Pattern.quote(string.toLowerCase()) + ".*");
    }

    private static boolean isValid(User user) {
        return user != null && !user.getIsBanned();
    }

    private static void sendPush(Push push, String title, String message) throws InternalServerErrorException {
        if (push.getType().equals("ios")) {
            // TODO: This won't work until we enable billing on GAE
            PushNotification pn = new PushNotification().setAlert(message).setDeviceTokens(push.getDeviceToken());
            PushNotificationService pns = new DefaultPushNotificationService();
            try {
                pns.send(pn, getConnection());
            } catch (ApnsException e) {
                throw new InternalServerErrorException("Communication error", e);
            }
        } else if (push.getType().equals("android")) {
            Sender sender = new Sender(GCM_API_KEY);
            try {
                sender.send(new com.google.android.gcm.server.Message.Builder().addData("title", title)
                        .addData("message", message).build(), push.getDeviceToken(), 3);
            } catch (IOException e) {
                throw new InternalServerErrorException("Communication error", e);
            }
        }
    }

    private static User getUser(DatastoreService ds, long id, User.Relation relation, boolean returnNull) throws NotFoundException {
        try {
            return new User(ds.get(KeyFactory.createKey("User", id)), relation);
        } catch (EntityNotFoundException e) {
            if (returnNull) {
                return null;
            } else {
                throw new NotFoundException("User not found", e);
            }
        }
    }

    private static User getUser(DatastoreService ds, String email, User.Relation relation, boolean returnNull) throws NotFoundException {
        Query query = new Query("User").setFilter(new Query.FilterPredicate(
                "email", Query.FilterOperator.EQUAL, email));
        PreparedQuery pq = ds.prepare(query);

        Entity e = pq.asSingleEntity();
        if (e == null) {
            if (returnNull) {
                return null;
            } else {
                throw new NotFoundException("Email not found");
            }
        } else {
            return new User(e, relation);
        }
    }

    private static Friend getFriend(DatastoreService ds, long myId, long theirId) {
        if (myId == theirId) {
            return null;
        }
        Query query = new Query("Friend").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, myId),
                new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, theirId)));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            query = new Query("Friend").setFilter(Query.CompositeFilterOperator.and(
                    new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, theirId),
                    new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, myId)));
            pq = ds.prepare(query);
            e = pq.asSingleEntity();
            if (e == null) {
                return null;
            }
        }
        return new Friend(e);
    }

    private static Community getCommunity(DatastoreService ds, long cid) throws NotFoundException {
        try {
            return new Community(ds.get(KeyFactory.createKey("Community", cid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Community not found", e);
        }
    }

    private static CommunityUser getCommunityUser(DatastoreService ds, long cid, long uid) {
        Query query = new Query("CommunityUser").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("cid", Query.FilterOperator.EQUAL, cid),
                new Query.FilterPredicate("uid", Query.FilterOperator.EQUAL, uid)));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new CommunityUser(e);
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
            // **Reminder** TODO: when we have more categories, this code might get more complex
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

    private static boolean isLastMatched(DatastoreService ds, long uidA, long uidB) {
        Query query = new Query("Episode").setFilter(new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, uidA))
                .addSort("startDate", Query.SortDirection.DESCENDING);
        PreparedQuery pq = ds.prepare(query);
        Iterator<Entity> iter = pq.asIterator(FetchOptions.Builder.withLimit(1));
        if (iter.hasNext()) {
            if (new Episode(iter.next()).getUid1() == uidB) {
                return true;
            }
        }
        return false;
    }

    private static Chat getChat(DatastoreService ds, long cid) throws NotFoundException {
        try {
            return new Chat(ds.get(KeyFactory.createKey("Chat", cid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Chat not found", e);
        }
    }

    private static Chat getChat(DatastoreService ds, long eofid, boolean isEpisode) {
        Query query = new Query("Chat").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("eofid", Query.FilterOperator.EQUAL, eofid),
                new Query.FilterPredicate("isEpisode", Query.FilterOperator.EQUAL, isEpisode)));
        // need to check isEpisode on the UNLIKELY chance that an episode and friend have same ID
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new Chat(e);
        }
    }

    private static Push getPush(DatastoreService ds, long pid) throws NotFoundException {
        try {
            return new Push(ds.get(KeyFactory.createKey("Push", pid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Push not found", e);
        }
    }

    private static Push getPush(DatastoreService ds, String type, String deviceToken) {
        Query query = new Query("Push").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("type", Query.FilterOperator.EQUAL, type),
                new Query.FilterPredicate("deviceToken", Query.FilterOperator.EQUAL, deviceToken)));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new Push(e);
        }
    }

    private static void sendEmail(User user, String subject, String message) throws InternalServerErrorException {
        sendEmail(user.getEmail(), user.getName(), subject, message);
    }

    private static void sendEmail(String email, String subject, String message) throws InternalServerErrorException {
        sendEmail(email, null, subject, message);
    }

    private static void sendEmail(String email, String name, String subject, String message) throws InternalServerErrorException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@samesies-app.appspotmail.com", "Samesies Admin"));
            if (name == null) {
                msg.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email));
            } else {
                msg.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email, name));
            }
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new InternalServerErrorException(e);
        }
    }

    public static ApnsConnectionFactory factory = null;

    private static void buildFactory(boolean isProd) throws ApnsException {
        DefaultApnsConnectionFactory.Builder builder = DefaultApnsConnectionFactory.Builder.get();
        if (isProd) {
            KeyStoreProvider ksp = new ClassPathResourceKeyStoreProvider("SamesiesProdAPN.p12", KeyStoreType.PKCS12, APN_CERTIFICATE_PASSWORD);
            builder.setProductionKeyStoreProvider(ksp);
        } else {
            KeyStoreProvider ksp = new ClassPathResourceKeyStoreProvider("SamesiesDevAPN.p12", KeyStoreType.PKCS12, APN_CERTIFICATE_PASSWORD);
            builder.setSandboxKeyStoreProvider(ksp);
        }
        factory = builder.build();
    }

    private static ApnsConnection getConnection() throws ApnsException {
        if (factory == null) {
            buildFactory(false);
        }
        return factory.openPushConnection();
    }

}
