package com.dfaenterprises.samesies.model;

/**
 * @author Ari Weiland
 */
public class Settings {

    private String mode;
    private Boolean matchMale;
    private Boolean matchFemale;
    private Boolean matchOther;

    public Settings() {
    }

    public Settings(String mode, Boolean matchMale, Boolean matchFemale, Boolean matchOther) {
        this.mode = mode;
        this.matchMale = matchMale;
        this.matchFemale = matchFemale;
        this.matchOther = matchOther;
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

    public static Settings defaults() {
        return new Settings("Random", true, true, true);
    }
}
