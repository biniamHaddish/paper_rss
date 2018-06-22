package com.biniisu.leanrss.models.inoreader;

import android.support.annotation.Keep;

/**
 * Created by biniam on 3/31/17.
 */
@Keep
public class InoReaderUserInfo {

    /**
     * userId : 1006371947
     * userName : benhaddish
     * userProfileId : 1006371947
     * userEmail : benhaddish@gmail.com
     * isBloggerUser : false
     * signupTimeSec : 1488955184
     * isMultiLoginEnabled : false
     */

    private String userId;
    private String userName;
    private String userProfileId;
    private String userEmail;
    private boolean isBloggerUser;
    private int signupTimeSec;
    private boolean isMultiLoginEnabled;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isIsBloggerUser() {
        return isBloggerUser;
    }

    public void setIsBloggerUser(boolean isBloggerUser) {
        this.isBloggerUser = isBloggerUser;
    }

    public int getSignupTimeSec() {
        return signupTimeSec;
    }

    public void setSignupTimeSec(int signupTimeSec) {
        this.signupTimeSec = signupTimeSec;
    }

    public boolean isIsMultiLoginEnabled() {
        return isMultiLoginEnabled;
    }

    public void setIsMultiLoginEnabled(boolean isMultiLoginEnabled) {
        this.isMultiLoginEnabled = isMultiLoginEnabled;
    }
}
