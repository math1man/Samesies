package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.*;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.*;

import java.util.*;

/**
 * @author Ari Weiland
 */
public class DS {

    public static DatastoreService getDS() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    public static User getUser(DatastoreService ds, long id, int relation, boolean returnNull) throws NotFoundException {
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

    /**
     * Retrieves all the specified users, excluding inactive users (unless ADMIN priority is invoked)
     * @param ds
     * @param ids
     * @param relation
     * @return
     */
    public static List<User> getUsers(DatastoreService ds, Set<Long> ids, int relation) {
        List<Key> keys = new ArrayList<>();
        for (Long id : ids) {
            if (id != null) {
                keys.add(KeyFactory.createKey("User", id));
            }
        }
        Map<Key, Entity> map = ds.get(keys);
        List<User> users = new ArrayList<>();
        for (Entity e : map.values()) {
            User u = new User(e, relation);
            if (relation == User.ADMIN || User.isActive(u)) {
                users.add(u);
            }
        }
        return users;
    }

    public static User getUser(DatastoreService ds, String email, int relation, boolean returnNull) throws NotFoundException {
        Query query = new Query("User").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
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

    public static Friend getFriend(DatastoreService ds, long myId, long theirId) {
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

    public static Community getCommunity(DatastoreService ds, long cid) throws NotFoundException {
        try {
            return new Community(ds.get(KeyFactory.createKey("Community", cid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Community not found", e);
        }
    }

    public static List<Community> getCommunitiesFromEmailSuffix(DatastoreService ds, String email) {
        String emailSuffix = email.substring(email.indexOf('@'));
        Query query = new Query("Community").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("type", Query.FilterOperator.EQUAL, Community.Type.EMAIL.name()),
                new Query.FilterPredicate("utilityString", Query.FilterOperator.EQUAL, emailSuffix)
        ));
        PreparedQuery pq = ds.prepare(query);
        List<Community> communities = new ArrayList<>();
        for (Entity e : pq.asIterable()) {
            communities.add(new Community(e));
        }
        return communities;
    }

    public static CommunityUser getCommunityUser(DatastoreService ds, long cid, long uid) {
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

    public static Question getQuestion(DatastoreService ds, long qid) throws NotFoundException {
        try {
            return new Question(ds.get(KeyFactory.createKey("Question", qid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Question not found", e);
        }
    }

    public static List<Long> getQids(DatastoreService ds, String mode) {
        List<Long> questions = new ArrayList<>();
        if (!mode.equals("Personal")) { // personal questions can be ignored at this stage
            // **Reminder** TODO: when we have more categories, this code might get more complex
            Query query = new Query("Question").setKeysOnly().setFilter(
                    new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, mode));
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

    public static Episode getEpisode(DatastoreService ds, long eid) throws NotFoundException {
        try {
            return new Episode(ds.get(KeyFactory.createKey("Episode", eid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Episode not found", e);
        }
    }

    public static boolean isLastMatched(DatastoreService ds, long uidA, long uidB) {
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

    public static Chat getChat(DatastoreService ds, long cid) throws NotFoundException {
        try {
            return new Chat(ds.get(KeyFactory.createKey("Chat", cid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Chat not found", e);
        }
    }

    public static Chat getChat(DatastoreService ds, long eofid, boolean isEpisode) {
        Query query = new Query("Chat").setFilter(Query.CompositeFilterOperator.and(
                new Query.FilterPredicate("eofid", Query.FilterOperator.EQUAL, eofid),
                new Query.FilterPredicate("isEpisode", Query.FilterOperator.EQUAL, isEpisode)));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new Chat(e);
        }
    }

    public static Push getPushById(DatastoreService ds, long pid) throws NotFoundException {
        try {
            return new Push(ds.get(KeyFactory.createKey("Push", pid)));
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Push not found", e);
        }
    }

    public static Push getPush(DatastoreService ds, long uid) {
        Query query = new Query("Push").setFilter(new Query.FilterPredicate("uid", Query.FilterOperator.EQUAL, uid));
        PreparedQuery pq = ds.prepare(query);
        Entity e = pq.asSingleEntity();
        if (e == null) {
            return null;
        } else {
            return new Push(e);
        }
    }

    public static Push getPush(DatastoreService ds, String type, String deviceToken) {
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

    public static void put(DatastoreService ds, Storable... ss) {
        put(ds, Arrays.asList(ss));
    }

    public static void put(DatastoreService ds, List<? extends Storable> ss) {
        List<Entity> es = new ArrayList<>();
        for (Storable s : ss) {
            if (s != null) {
                es.add(s.toEntity());
            }
        }
        ds.put(es);
        for (int i=0; i<es.size(); i++) {
            ss.get(i).setId(es.get(i).getKey().getId());
        }
    }
}
