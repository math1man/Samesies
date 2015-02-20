package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.Storable;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class Utils {

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

    public static void put(DatastoreService ds, Storable s) {
        Entity entity = s.toEntity();
        ds.put(entity);
        s.setId(entity.getKey().getId());
    }

    public static Query.Filter makeDoubleFilter(Query.CompositeFilterOperator compOp,
                                                String prop1, Query.FilterOperator op1, Object value1,
                                                String prop2, Query.FilterOperator op2, Object value2) {
        return new Query.CompositeFilter(compOp, Arrays.asList(
                (Query.Filter) new Query.FilterPredicate(prop1, op1, value1),
                new Query.FilterPredicate(prop2, op2, value2)));
    }
}
