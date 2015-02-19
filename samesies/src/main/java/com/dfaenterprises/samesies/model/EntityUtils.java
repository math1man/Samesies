package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.EmbeddedEntity;

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
}
