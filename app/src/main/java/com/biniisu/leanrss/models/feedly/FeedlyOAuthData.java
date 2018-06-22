package com.biniisu.leanrss.models.feedly;

/**
 * Created by biniam on 5/8/17.
 */

public class FeedlyOAuthData {
    /**
     * expires_in : 3920
     * token_type : Bearer
     * refresh_token : AQAA7rJ7InAiOjEsImEiOiJmZWVk...
     * id : c805fcbf-3acf-4302-a97e-d82f9d7c897f
     * state : ...
     * plan : standard
     * access_token : AQAAF4iTvPam_M4_dWheV_5NUL8E...
     */

    private int expires_in;
    private String token_type;
    private String refresh_token;
    private String id;
    private String state;
    private String plan;
    private String access_token;

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
