package com.biniam.rss.models.feedWragler;

/**
 * Created by biniam on 12/23/17.
 */

public class FeedWranglerAuthorizationItems {

    /**
     * access_token : c3df0fce5ec5e6316e83e47f007492b4
     * user : {"email":"test","account_status":"active","read_later_service":"none"}
     * error : null
     * result : success
     */

    private String access_token;
    private UserBean user;
    private Object error;
    private String result;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public static class UserBean {
        /**
         * email : test
         * account_status : active
         * read_later_service : none
         */

        private String email;
        private String account_status;
        private String read_later_service;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAccount_status() {
            return account_status;
        }

        public void setAccount_status(String account_status) {
            this.account_status = account_status;
        }

        public String getRead_later_service() {
            return read_later_service;
        }

        public void setRead_later_service(String read_later_service) {
            this.read_later_service = read_later_service;
        }
    }
}
