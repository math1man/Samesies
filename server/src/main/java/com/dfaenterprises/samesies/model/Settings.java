package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.GeoPt;

/**
 * @author Ari Weiland
 */
public class Settings {

    private String mode;
    private Boolean matchMale;
    private Boolean matchFemale;
    private Boolean matchOther;
    private GeoPt location;
    private String community;

    public Settings() {
    }

    public Settings(String mode) {
        this(mode, null, null, null, null, null);
    }

    public Settings(String mode, Boolean matchMale, Boolean matchFemale, Boolean matchOther, GeoPt location, String community) {
        this.mode = mode;
        this.matchMale = matchMale;
        this.matchFemale = matchFemale;
        this.matchOther = matchOther;
        this.location = location;
        this.community = community;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean getMatchMale() {
        return matchMale;
    }

    public void setMatchMale(Boolean matchMale) {
        this.matchMale = matchMale;
    }

    public Boolean getMatchFemale() {
        return matchFemale;
    }

    public void setMatchFemale(Boolean matchFemale) {
        this.matchFemale = matchFemale;
    }

    public Boolean getMatchOther() {
        return matchOther;
    }

    public void setMatchOther(Boolean matchOther) {
        this.matchOther = matchOther;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public boolean hasLocation() {
        return getLocation() != null;
    }

    public boolean hasCommunity() {
        return getCommunity() != null;
    }

    public static Settings defaults() {
        return new Settings("Random", true, true, true, null, null);
    }
}
