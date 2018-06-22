package com.biniam.rss.ui.viewmodels;

import android.arch.persistence.room.Ignore;

public class NavigationTagItemModel {

    public String name;
    @Ignore
    public int tagUnreadCount;

    public NavigationTagItemModel() {
    }

    public NavigationTagItemModel(String name, int tagUnreadCount) {
        this.name = name;
        this.tagUnreadCount = tagUnreadCount;
    }
}