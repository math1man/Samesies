package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class Nominal extends Storable {

    private String nominal;
    private boolean isName;

    public Nominal() {
    }

    public Nominal(Entity e) {
        String key = e.getKey().getName();
        setId(Long.parseLong(StringUtils.substring(key, 0, -1)));
        isName = (key.charAt(key.length() - 1) == 'n');
        nominal = (String) e.getProperty("nominal");
    }

    private Nominal(Long id, String nominal, boolean isName) {
        setId(id);
        this.nominal = StringUtils.lowerCase(nominal);
        this.isName = isName;
    }

    public static List<Nominal> getNominals(User user) {
        Long id = user.getId();
        List<Nominal> nominals = new ArrayList<>();
        nominals.add(new Nominal(id, user.getAlias(), false));
        String name = user.getName();
        if (name != null) {
            nominals.add(new Nominal(id, name, true));
        }
        return nominals;
    }

    public String getNominal() {
        return nominal;
    }

    public void setNominal(String nominal) {
        this.nominal = nominal;
    }

    public boolean isName() {
        return isName;
    }

    public void setName(boolean isName) {
        this.isName = isName;
    }

    @Override
    public Entity toEntity() {
        Entity e;
        if (getId() == null) {
            e = new Entity("Nominal");
        } else {
            e = new Entity("Nominal", getId() + (isName ? "n" : "a"));
        }
        e.setProperty("nominal", nominal);
        return e;
    }
}
