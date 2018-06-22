package com.biniisu.leanrss.ui.controllers;

import com.biniisu.leanrss.ui.viewmodels.NavigationSubscriptionItemModel;

public interface NavigationSelectionListener {
    void onNavigationEverythingSelected();

    void onNavigationTagSelected(String tagName);

    void onNavigationSubscriptionSelected(NavigationSubscriptionItemModel subscriptionEntity);
}