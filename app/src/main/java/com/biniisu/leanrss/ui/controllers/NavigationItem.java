package com.biniisu.leanrss.ui.controllers;

import com.biniisu.leanrss.expandablerv.ExpandableGroup;
import com.biniisu.leanrss.ui.viewmodels.NavigationSubscriptionItemModel;
import com.biniisu.leanrss.ui.viewmodels.NavigationTagItemModel;

import java.util.List;

public class NavigationItem extends ExpandableGroup<NavigationSubscriptionItemModel> {

    private String title;
    private NavigationSubscriptionItemModel untaggedSubscriptionEntity;
    private boolean everything;
    private int unreadCount;
    private List<Integer> subsUnreadCount;

    public NavigationItem(NavigationTagItemModel navigationTagItemModel, List<NavigationSubscriptionItemModel> items, List<Integer> subsUnreadCount, NavigationSubscriptionItemModel untaggedSubscriptionEntity) {
        super(navigationTagItemModel.name, items);
        this.title = navigationTagItemModel.name;
        this.untaggedSubscriptionEntity = untaggedSubscriptionEntity;
        this.unreadCount = navigationTagItemModel.tagUnreadCount;
        this.subsUnreadCount = subsUnreadCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NavigationItem)) return false;

        return this.title == ((NavigationItem) obj).title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public NavigationSubscriptionItemModel getUntaggedSubscriptionEntity() {
        return untaggedSubscriptionEntity;
    }

    public boolean isSubscription() {
        return untaggedSubscriptionEntity != null;
    }

    public boolean isAllSubscriptions() {
        return everything;
    }

    public void setAllSubscriptions(boolean everything) {
        this.everything = everything;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public List<Integer> getSubsUnreadCount() {
        return subsUnreadCount;
    }
}