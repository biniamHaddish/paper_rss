package com.biniam.rss.models.inoreader;

import android.support.annotation.Keep;

/**
 * Created by biniam on 3/23/17.
 */
@Keep
public class InoReaderAuthItems {

    /**
     * access_token : [ACCESS_TOKEN]
     * token_type : Bearer
     * expires_in : ["EXPIRATION_IN_SECONDS"]
     * refresh_token : [REFRESH_TOKEN]
     * scope : read
     */

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String scope;
    private long expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }
}
