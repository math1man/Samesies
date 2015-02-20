package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public interface Storable {

    public Long getId();

    public void setId(Long id);

    public Entity toEntity();

}
