package com.dfaenterprises.samesies;

import apns.*;
import com.dfaenterprises.samesies.apn.ApnManager;
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
@SuppressWarnings("UnusedDeclaration")
@Api(
        name = "samesies",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class SamesiesApi {

    public void initQuestions() throws ServiceException {
        DatastoreService ds = DS.getDS();

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
        DS.put(ds, questions);

        ds.put(Arrays.asList(new Entity("Category", "All"),
                new Entity("Category", "Random")));
    }

    public void initModes() throws ServiceException {
        DatastoreService ds = DS.getDS();
        DS.put(ds, new Mode("Random", "Answer 10 random questions from our database."),
                new Mode("Personal", "Answer each of your and your partner's 5 personal questions."));
    }

    //----------------------------
    //        User Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.login",
            path = "user/login",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public User login(User user) throws ServiceException {
        DatastoreService ds = DS.getDS();

        String email = user.getEmail();
        String password = user.getPassword();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        User dsUser = DS.getUser(ds, email, User.Relation.SELF, false);
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
        DatastoreService ds = DS.getDS();

        String email = newUser.getEmail();
        if (email == null) {
            throw new BadRequestException("Invalid Email");
        }
        if (newUser.getPassword() == null) {
            throw new BadRequestException("Invalid Password");
        }
        if (DS.getUser(ds, email, User.Relation.STRANGER, true) == null) {
            newUser.initNewUser();
            DS.put(ds, newUser);
            DS.put(ds, new CommunityUser(Constants.EVERYONE_CID, newUser.getId(), true));
            sendEmail(newUser, "Activate your Samesies Account",
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
        DatastoreService ds = DS.getDS();
        User user = DS.getUser(ds, email, User.Relation.ADMIN, false);
        String tempPass = EntityUtils.randomString(8);
        user.setPassword(tempPass);
        DS.put(ds, user);
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
        return DS.getUser(DS.getDS(), uid, User.Relation.STRANGER, false);
    }

    @ApiMethod(name = "samesiesApi.getUsers",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<User> getUsers(@Named("ids") long[] uids) throws ServiceException {
        List<Key> keys = new ArrayList<>();
        for (long uid : uids) {
            keys.add(KeyFactory.createKey("User", uid));
        }
        Map<Key, Entity> map = DS.getDS().get(keys);
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
        DatastoreService ds = DS.getDS();
        if (user.getId() == null) {
            throw new BadRequestException("User ID not specified");
        }
        String newPassword = user.getNewPassword();
        User dsUser = DS.getUser(ds, user.getId(), User.Relation.SELF, false);
        if (newPassword != null) {
            // password is being changed
            if (BCrypt.checkpw(user.getPassword(), dsUser.getHashedPw())) {
                user.setPassword(newPassword);
            } else {
                throw new BadRequestException("Invalid Password");
            }
        }
        user.setLocation(dsUser.getLocation());
        user.setIsActivated(dsUser.getIsActivated());
        user.setIsBanned(dsUser.getIsBanned());
        DS.put(ds, user);
    }

    @ApiMethod(name = "samesiesApi.findUser",
            path = "user/find/{email}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public User findUser(@Named("email") String email) throws ServiceException {
        User user = DS.getUser(DS.getDS(), email, User.Relation.STRANGER, true);
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
        DatastoreService ds = DS.getDS();
        // first, lets check if they straight entered an email
        User user = DS.getUser(DS.getDS(), string, User.Relation.STRANGER, true);
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
        DatastoreService ds = DS.getDS();
        Flag flag = new Flag(flaggedId, flaggerId, reason);
        DS.put(ds, flag);
    }

    @ApiMethod(name = "samesiesApi.banUser",
            path = "user/{id}/ban",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void banUser(@Named("id") long uid, @Nullable @Named("isBanned") Boolean isBanned) throws ServiceException {
        DatastoreService ds = DS.getDS();
        if (isBanned == null) {
            isBanned = true;
        }
        User user = DS.getUser(ds, uid, User.Relation.ADMIN, false);
        user.setIsBanned(isBanned);
        DS.put(ds, user);
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
        DatastoreService ds = DS.getDS();

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
                User user = DS.getUser(ds, theirUid, relation, true);
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
        return DS.getFriend(DS.getDS(), myId, theirId);
    }

    @ApiMethod(name = "samesiesApi.addFriend",
            path = "friends/add/{myId}/{theirId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Friend addFriend(@Named("myId") long myId, @Named("theirId") long theirId) throws ServiceException {
        DatastoreService ds = DS.getDS();

        if (myId == theirId) {
            throw new ForbiddenException("Cannot add oneself as a friend");
        }

        Friend.Status sendPush = null;

        Friend friend = DS.getFriend(ds, myId, theirId);
        if (friend == null) {
            friend = new Friend(myId, theirId);
            sendPush = friend.getStatus();
        } else if (friend.isUid1(myId)) {
            if (friend.getStatus() == Friend.Status.DELETED_1) {
                // if you had deleted them, set it back to pending
                friend.setStatus(Friend.Status.PENDING);
                sendPush = friend.getStatus();
            }
            // if already pending, accepted, or deleted by them, leave it
        } else {
            if (friend.getStatus() == Friend.Status.PENDING) {
                // if both have added each other, accept
                friend.setStatus(Friend.Status.ACCEPTED);
                sendPush = friend.getStatus();
            } else if (friend.getStatus() == Friend.Status.DELETED_2) {
                // if you had deleted them, set it back to pending
                friend.setUid1(myId);
                friend.setUid2(theirId);
                friend.setStatus(Friend.Status.PENDING);
                sendPush = friend.getStatus();
            }
            // if already accepted or deleted by them, leave it
        }
        if (sendPush == Friend.Status.PENDING) {
            sendPairingPush(ds, myId, theirId, "New Friend Request", "wants to be your friend!");
        } else if (sendPush == Friend.Status.ACCEPTED) {
            sendPairingPush(ds, myId, theirId, "Friend Request Accepted", "accepted your friend request!");
        }
        if (friend.getStatus().isDeleted()) {
            return null;
        } else {
            DS.put(ds, friend); // update the friend in case it changed
            friend.setUser(DS.getUser(ds, theirId, friend.getStatus().getRelation(), true));
            return friend;
        }
    }

    @ApiMethod(name = "samesiesApi.removeFriend",
            path = "friends/{id}/remove/{myId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeFriend(@Named("id") long fid, @Named("myId") long myId) throws ServiceException {
        DatastoreService ds = DS.getDS();
        try {
            Friend friend = new Friend(ds.get(KeyFactory.createKey("Friend", fid)));
            if (friend.isUid1(myId)) {
                friend.setStatus(Friend.Status.DELETED_1);
            } else {
                friend.setStatus(Friend.Status.DELETED_2);
            }
            DS.put(ds, friend);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Friend not found", e);
        }
    }

    //----------------------------
    //      Community Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getCommunity",
            path = "community/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Community getCommunity(@Named("id") Long cid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        List<User> users = new ArrayList<>();
        Community community = DS.getCommunity(ds, cid);
        Query query = new Query("CommunityUser").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("cid", Query.FilterOperator.EQUAL, cid),
                new Query.FilterPredicate("isValidated", Query.FilterOperator.EQUAL, true)));
        PreparedQuery pq = ds.prepare(query);
        for (Entity e : pq.asIterable()) {
            User user = DS.getUser(ds, new CommunityUser(e).getUid(), User.Relation.STRANGER, true);
            if (isValid(user)) {
                users.add(user);
            }
        }
        Collections.shuffle(users);
        if (users.size() > 100) {
            // only return 100 users max
            // need this out here for randomization reasons
            community.setUsers(users.subList(0, 100));
        } else {
            community.setUsers(users);
        }
        return community;
    }

    @ApiMethod(name = "samesiesApi.getNearBy",
            path = "nearby/{latitude}/{longitude}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Community getNearBy(@Named("latitude") float latitude, @Named("longitude") float longitude) throws ServiceException {
        DatastoreService ds = DS.getDS();
        GeoPt location = new GeoPt(latitude, longitude);
        Query query = new Query("User").setFilter(new Query.FilterPredicate("isBanned", Query.FilterOperator.EQUAL, false));
        PreparedQuery pq = ds.prepare(query);
        List<User> users = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            User user = new User(e, User.Relation.STRANGER);
            if (user.hasLocation() && distance(user.getLocation(), location) <= 10) {
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
        DatastoreService ds = DS.getDS();
        Query query = new Query("CommunityUser").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("uid", Query.FilterOperator.EQUAL, uid),
                new Query.FilterPredicate("isActive", Query.FilterOperator.EQUAL, true),
                new Query.FilterPredicate("isValidated", Query.FilterOperator.EQUAL, true)));
        PreparedQuery pq = ds.prepare(query);
        List<Community> communities = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Community community = DS.getCommunity(ds, new CommunityUser(e).getCid());
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
        DatastoreService ds = DS.getDS();
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
            path = "communities/{id}/join/{myId}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Community joinCommunity(@Named("id") long cid, @Named("myId") long myUid, @Nullable@Named("string") String string) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Community community = DS.getCommunity(ds, cid);
        CommunityUser cu = DS.getCommunityUser(ds, cid, myUid);
        boolean returnNull = true;
        if (cu == null) {
            // CommunityUser does not exist, make new one
            switch (community.getType()) {
                case OPEN:
                    cu = new CommunityUser(community.getId(), myUid, true);
                    returnNull = false;
                    break;
                case EMAIL:
                    // utility string is an email domain
                    if (string != null && string.contains(community.getUtilityString())) {
                        cu = new CommunityUser(community.getId(), myUid);
                        sendEmail(string, "Join " + community.getName(),
                                "Click the link below to join the Samesies community for " + community.getName() + ":\n" +
                                        "https://samesies-app.appspot.com/_ah/spi/communities/join?community_user_id=" + cu.getId() + "\n\n" +
                                        "Have fun,\n" +
                                        "The Samesies Team");
                        returnNull = false;
                    }
                    break;
                case PASSWORD:
                    // utility string is a password
                    if (string != null && BCrypt.checkpw(string, community.getUtilityString())) {
                        cu = new CommunityUser(community.getId(), myUid, true);
                        returnNull = false;
                    }
                    break;
                case EXCLUSIVE:
                    cu = new CommunityUser(community.getId(), myUid);
                    break;
            }
        } else {
            // CommunityUser already exists, return community if validated
            cu.setIsActive(true);
            returnNull = !cu.getIsValidated();
        }
        if (cu != null) {
            DS.put(ds, cu);
        }
        if (returnNull) {
            return null;
        } else {
            return community;
        }
    }

    @ApiMethod(name = "samesiesApi.leaveCommunity",
            path = "communities/{id}/leave/{myId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void leaveCommunity(@Named("id") long cid, @Named("myId") long myUid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        CommunityUser cu = DS.getCommunityUser(ds, cid, myUid);
        if (cu == null) {
            throw new NotFoundException("User is not member of that community");
        } else {
            cu.setIsActive(false);
            DS.put(ds, cu);
        }
    }

    @ApiMethod(name = "samesiesApi.createOpenCommunity",
            path = "communities/create/{name}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createOpenCommunity(@Named("name") String name, @Nullable@Named("description") String description) throws ServiceException {
        DS.put(DS.getDS(), new Community(name, description));
    }

    @ApiMethod(name = "samesiesApi.createEmailCommunity",
            path = "communities/create/email/{name}/{emailSuffix}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createEmailCommunity(@Named("name") String name, @Named("emailSuffix") String emailSuffix,
                                     @Nullable@Named("description") String description) throws ServiceException {
        DS.put(DS.getDS(), new Community(name, description, Community.Type.EMAIL, emailSuffix));
    }

    @ApiMethod(name = "samesiesApi.createPasswordCommunity",
            path = "communities/create/password/{name}/{password}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createPasswordCommunity(@Named("name") String name, @Named("password") String password,
                                        @Nullable@Named("description") String description) throws ServiceException {
        DS.put(DS.getDS(), new Community(name, description, Community.Type.PASSWORD,
                BCrypt.hashpw(password, BCrypt.gensalt())));
    }

    @ApiMethod(name = "samesiesApi.createExclusiveCommunity",
            path = "communities/create/exclusive/{name}/{password}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void createExclusiveCommunity(@Named("name") String name, @Named("password") String adminPassword,
                                        @Nullable@Named("description") String description) throws ServiceException {
        DS.put(DS.getDS(), new Community(name, description, Community.Type.EXCLUSIVE,
                BCrypt.hashpw(adminPassword, BCrypt.gensalt())));
    }

    //----------------------------
    //       Question Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getQuestion",
            path = "question/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Question getQuestion(@Named("id") long qid) throws ServiceException {
        return DS.getQuestion(DS.getDS(), qid);
    }

    @ApiMethod(name = "samesiesApi.getQuestions",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getQuestions() throws ServiceException {
        DatastoreService ds = DS.getDS();
        Query query = new Query("Question")
                .addProjection(new PropertyProjection("q", String.class))
                .addProjection(new PropertyProjection("category", String.class));
        PreparedQuery pq = ds.prepare(query);
        List<Question> questions = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Question question = new Question(e);
            if (!question.getCategory().equals("Suggestion")) {
                questions.add(question);
            }
        }
        return questions;
    }

    @ApiMethod(name = "samesiesApi.getCategories",
            path = "questions/categories",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<String> getCategories() throws ServiceException {
        DatastoreService ds = DS.getDS();

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
        DS.put(DS.getDS(), q);
    }

    //----------------------------
    //       Episode Calls
    //----------------------------

    @ApiMethod(name = "samesiesApi.getModes",
            path = "episodes/modes",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Mode> getModes() throws ServiceException {
        DatastoreService ds = DS.getDS();
        Query query = new Query("Mode");
        PreparedQuery pq = ds.prepare(query);
        List<Mode> modes = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            modes.add(new Mode(e));
        }
        return modes;
    }

    @ApiMethod(name = "samesiesApi.findEpisode",
            path = "episode/find/{myId}/{mode}/{matchMale}/{matchFemale}/{matchOther}/{isPersistent}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Episode findEpisode(@Named("myId") long myUid, @Named("mode") String mode, @Named("matchMale") boolean matchMale,
                               @Named("matchFemale") boolean matchFemale, @Named("matchOther") boolean matchOther,
                               @Named("isPersistent") Boolean isPersistent, @Nullable@Named("cid") Long cid,
                               @Nullable@Named("latitude") Float latitude, @Nullable@Named("longitude") Float longitude) throws ServiceException {
        DatastoreService ds = DS.getDS();
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
                    DS.put(ds, temp);
                }
            }
        }
        if (episode == null) {
            episode = new Episode(myUid, isPersistent, settings);
        } else {
            episode.setStatus(Episode.Status.IN_PROGRESS);
            episode.setUid2(myUid);
            episode.setQids(DS.getQids(ds, mode));
            episode.setUser(DS.getUser(ds, episode.getUid1(), User.Relation.STRANGER, true));
            episode.modify();
        }
        DS.put(ds, episode);
        return episode;
    }

    @ApiMethod(name = "samesiesApi.connectEpisode",
            path = "episode/connect/{myId}/{theirId}/{mode}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Episode connectEpisode(@Named("myId") long myUid, @Named("theirId") long theirUid, @Named("mode") String mode) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = new Episode(myUid, theirUid, new Settings(mode));
        episode.setQids(DS.getQids(ds, mode));
        DS.put(ds, episode);
        sendPairingPush(ds, myUid, theirUid, "New Connection", "wants to connect!");
        return episode;
    }

    @ApiMethod(name = "samesiesApi.acceptEpisode",
            path = "episode/{id}/accept",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void acceptEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = DS.getEpisode(ds, eid);
        episode.setStatus(Episode.Status.IN_PROGRESS);
        episode.modify();
        DS.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.getEpisode",
            path = "episode/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Episode getEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = DS.getEpisode(ds, eid);
        if (!episode.getIsPersistent() && episode.getStatus() == Episode.Status.MATCHING) {
            // update it for matching purposes so that the matching system can discard old episodes
            episode.setLastModified(new Date());
            DS.put(ds, episode);
        }
        return episode;
    }

    @ApiMethod(name = "samesiesApi.answerEpisode",
            path = "episode/{id}/answer/{myId}/{answer}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Episode answerEpisode(@Named("id") long eid, @Named("myId") long myUid,
                                 @Named("answer") String answer) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = DS.getEpisode(ds, eid);
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
        DS.put(ds, episode, a);
        sendPairingPush(ds, myUid, episode.getOtherUid(myUid), "New Answer", "answered a question in your connection!");
        return episode;
    }

    @ApiMethod(name = "samesiesApi.endEpisode",
            path = "episode/{id}/end",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void endEpisode(@Named("id") long eid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = DS.getEpisode(ds, eid);
        if (episode.getStatus() == Episode.Status.MATCHING) {
            episode.setStatus(Episode.Status.UNMATCHED);
        } else if (episode.getAnswers1().size() == 10 && episode.getAnswers2().size() == 10) {
            episode.setStatus(Episode.Status.COMPLETE);
        } else {
            episode.setStatus(Episode.Status.ABANDONED);
        }
        episode.modify();
        DS.put(ds, episode);
    }

    @ApiMethod(name = "samesiesApi.getConnections",
            path = "connections/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Episode> getConnections(@Named("id") long uid) throws ServiceException {
        DatastoreService ds = DS.getDS();

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
                    User user = DS.getUser(ds, episode.getOtherUid(uid), User.Relation.STRANGER, true);
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
            path = "episodes/{eid}/questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Question> getEpisodeQuestions(@Named("eid") long eid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Episode episode = DS.getEpisode(ds, eid);
        List<Question> questions = new ArrayList<>();
        if (episode.isPersonal()) {
            User u1 = DS.getUser(ds, episode.getUid1(), User.Relation.ADMIN, false);
            User u2 = DS.getUser(ds, episode.getUid2(), User.Relation.ADMIN, false);
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
                questions.add(DS.getQuestion(ds, qid));
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
        DatastoreService ds = DS.getDS();
        Chat chat = DS.getChat(ds, eofid, isEpisode);
        if (chat == null) {
            chat = new Chat(eofid, isEpisode, myUid, theirUid);
        } else {
            chat.setIsClosed(false);
            chat.setIsUpToDate(myUid, true);
        }
        DS.put(ds, chat);
        return chat;
    }

    @ApiMethod(name = "samesiesApi.getChat",
            path = "chat/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Chat getChat(@Named("id") long cid) throws ServiceException {
        return DS.getChat(DS.getDS(), cid);
    }

    @ApiMethod(name = "samesiesApi.getChats",
            path = "chats/{myId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Chat> getChats(@Named("myId") long myUid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Query query = new Query("Chat").setFilter(Query.CompositeFilterOperator.and(
                Query.CompositeFilterOperator.or(
                        new Query.FilterPredicate("uid1", Query.FilterOperator.EQUAL, myUid),
                        new Query.FilterPredicate("uid2", Query.FilterOperator.EQUAL, myUid)),
                new Query.FilterPredicate("isClosed", Query.FilterOperator.EQUAL, false)));
        PreparedQuery pq = ds.prepare(query);
        List<Chat> chats = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            Chat chat = new Chat(e);
            User user = DS.getUser(ds, chat.getOtherUid(myUid), User.Relation.STRANGER, true);
            if (isValid(user)) {
                chat.setUser(user);
                chats.add(chat);
            }
        }
        return chats;
    }

    @ApiMethod(name = "samesiesApi.updateChat",
            path = "chat/{id}/update/{eofid}/{isEpisode}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateChat(@Named("id") long cid, @Named("eofid") long eofid, @Named("isEpisode") boolean isEpisode) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Chat chat = DS.getChat(ds, cid);
        chat.setEofid(eofid);
        chat.setIsEpisode(isEpisode);
        DS.put(ds, chat);
    }

    @ApiMethod(name = "samesiesApi.closeChat",
            path = "chat/{id}/close",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void closeChat(@Named("id") long cid) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Chat chat = DS.getChat(ds, cid);
        chat.setIsClosed(true);
        DS.put(ds, chat);
    }

    @ApiMethod(name = "samesiesApi.sendMessage",
            path = "chat/message/{chatId}/{myId}/{message}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Message sendMessage(@Named("chatId") long cid, @Named("myId") long myUid, @Named("message") String message,
                               @Named("random") @Nullable String random) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Chat chat = DS.getChat(ds, cid);
        Message m = new Message(cid, myUid, message, random);
        chat.update(myUid, m.getSentDate());
        DS.put(ds, chat, m);
        sendPairingPush(ds, myUid, chat.getOtherUid(myUid), "New Message", "sent you a message!");
        return m;
    }

    @ApiMethod(name = "samesiesApi.getMessages",
            path = "chat/messages/{chatId}/{after}/{myId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<Message> getMessages(@Named("chatId") long cid, @Named("after") Date after, @Named("myId") long myUid) throws ServiceException {
        DatastoreService ds = DS.getDS();

        Chat chat = DS.getChat(ds, cid);
        chat.setIsUpToDate(myUid, true);
        DS.put(ds, chat);

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
        DS.put(DS.getDS(), feedback);
    }

    @ApiMethod(name = "samesiesApi.sendEmail",
            path = "email/{uid}/{subject}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendEmail(@Named("uid") long uid, @Named("subject") String subject, @Named("message") String[] messageLines) throws ServiceException {
        StringBuilder sb = new StringBuilder();
        for (String s : messageLines) {
            sb.append(s).append('\n');
        }
        sendEmail(DS.getUser(DS.getDS(), uid, User.Relation.ADMIN, false), subject, sb.toString());
    }

    @ApiMethod(name = "samesiesApi.registerPush",
            path = "push/register/{id}/{type}/{deviceToken}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Push registerPush(@Named("id") long uid, @Named("type") String type, @Named("deviceToken") String deviceToken) throws ServiceException {
        DatastoreService ds = DS.getDS();
        type = type.toLowerCase();
        Push devicePush = DS.getPush(ds, type, deviceToken);
        Push userPush = DS.getPush(ds, uid);
        if (devicePush == null && userPush == null) {
            // neither device nor user is registered:
            // create a new push
            devicePush = new Push(uid, type, deviceToken);
        } else if (devicePush == null) {
            // device is not registered, user is:
            // change the device token of the user push
            userPush.setDeviceToken(deviceToken);
            devicePush = userPush;
        } else if (userPush == null) {
            // user is not registered, device is:
            // change the uid of the device push
            devicePush.setUid(uid);
        } else if (!userPush.equals(devicePush)) {
            // both are registered separately:
            // change the uid of the device push, delete the old user push
            ds.delete(userPush.toEntity().getKey());
            devicePush.setUid(uid);
        } // else user push and device push match: user-device combo is registered together
        DS.put(ds, devicePush);
        return devicePush;
    }

    @ApiMethod(name = "samesiesApi.sendPush",
            path = "push/{id}/send/{title}/{message}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void sendPush(@Named("id") long pid, @Named("title") String title, @Named("message") String message) throws ServiceException {
        DatastoreService ds = DS.getDS();
        Push push = DS.getPushById(ds, pid);
        sendPush(push, title, message);
    }

    //----------------------------
    //   Static Helper Methods
    //----------------------------

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
        if (DS.isLastMatched(ds, uid1, uid2) || DS.isLastMatched(ds, uid2, uid1)) {
            return false;
        }

        // finally check that genders are acceptable to each other
        return checkGender(DS.getUser(ds, uid1, User.Relation.ADMIN, false).getGender(), settings2)
                && checkGender(DS.getUser(ds, uid2, User.Relation.ADMIN, false).getGender(), settings1);
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

    private static void sendPairingPush(DatastoreService ds, long myUid, long theirUid, String title, String messagePredicate) throws ServiceException {
        Push push = DS.getPush(ds, theirUid);
        User me = DS.getUser(ds, myUid, User.Relation.SELF, false);
        sendPush(push, title, me.getDisplayName() + " " + messagePredicate);
    }

    private static void sendPush(Push push, String title, String message) throws InternalServerErrorException {
        if (push != null) {
            if (push.getType().equals("ios")) {
                PushNotification pn = new PushNotification().setAlert(message).setBadge(1).setDeviceTokens(push.getDeviceToken());
                PushNotificationService pns = new DefaultPushNotificationService();
                ApnsConnection conn;
                try {
                    conn = ApnManager.getPushConnection(Constants.IS_PROD);
                    pns.send(pn, conn);
                } catch (ApnsException e) {
                    throw new InternalServerErrorException("Communication error: iOS", e);
                } catch (IOException e) {
                    throw new InternalServerErrorException("File IO error", e);
                }
            } else if (push.getType().equals("android")) {
                Sender sender = new Sender(Constants.GCM_API_KEY);
                try {
                    sender.sendNoRetry(new com.google.android.gcm.server.Message.Builder().addData("title", title)
                            .addData("message", message).build(), push.getDeviceToken());
                } catch (IOException e) {
                    throw new InternalServerErrorException("Communication error: Android", e);
                }
            }
        }
    }

    private static void sendEmail(User user, String subject, String message) throws InternalServerErrorException {
        sendEmail(user.getEmail(), user.getDisplayName(), subject, message);
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
}
