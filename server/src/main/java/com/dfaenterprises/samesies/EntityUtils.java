package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.Storable;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class EntityUtils {

    public static <T> EmbeddedEntity listToEntity(List<T> ts) {
        EmbeddedEntity e = new EmbeddedEntity();
        if (ts != null) {
            for (int i=0; i<ts.size(); i++) {
                e.setProperty("" + i, ts.get(i));
            }
        }
        return e;
    }

    public static <T> List<T> entityToList(Object obj, int maxLength, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (obj != null) {
            EmbeddedEntity e = (EmbeddedEntity) obj;
            for (int i=0; i<maxLength; i++) {
                if (e.hasProperty("" + i)) {
                    list.add(clazz.cast(e.getProperty("" + i)));
                }
            }
        }
        return list;
    }

    public static void put(DatastoreService ds, Storable... ss) {
        List<Entity> es = new ArrayList<>();
        for (Storable s : ss) {
            if (s != null) {
                es.add(s.toEntity());
            }
        }
        ds.put(es);
        for (int i=0; i<es.size(); i++) {
            ss[i].setId(es.get(i).getKey().getId());
        }
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i=0; i<length; i++) {
            sb.append(possible.charAt((int) (Math.random() * possible.length())));
        }
        return sb.toString();
    }
}
