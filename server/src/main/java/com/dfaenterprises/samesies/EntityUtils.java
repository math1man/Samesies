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

    public static void setListProp(Entity e, String property, List<?> list) {
        EmbeddedEntity ee = new EmbeddedEntity();
        if (list != null) {
            for (int i=0; i<list.size(); i++) {
                ee.setProperty("" + i, list.get(i));
            }
        }
        e.setProperty(property, ee);
    }

    public static <T> List<T> getListProp(Entity e, String property, int maxLength, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Object obj = e.getProperty(property);
        if (obj != null) {
            EmbeddedEntity ee = (EmbeddedEntity) obj;
            for (int i=0; i<maxLength; i++) {
                if (ee.hasProperty("" + i)) {
                    list.add(clazz.cast(ee.getProperty("" + i)));
                }
            }
        }
        return list;
    }

    public static void setEnumProp(Entity e, String property, Enum<?> object, boolean isIndexed) {
        if (isIndexed) {
            e.setProperty(property, object.name());
        } else {
            e.setUnindexedProperty(property, object.name());
        }
    }

    public static <T extends Enum<T>> T getEnumProp(Entity e, String property, Class<T> clazz) {
        return Enum.valueOf(clazz, (String) e.getProperty(property));
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
