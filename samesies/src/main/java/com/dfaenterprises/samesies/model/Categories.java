package com.dfaenterprises.samesies.model;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class Categories {

    private List<String> categories;

    public Categories() {
    }

    public Categories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
