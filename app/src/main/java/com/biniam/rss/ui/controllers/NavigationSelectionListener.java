package com.biniam.rss.ui.controllers;

import com.biniam.rss.ui.viewmodels.NavigationSubscriptionItemModel;

public interface NavigationSelectionListener {
    void onNavigationEverythingSelected();

    void onNavigationTagSelected(String tagName);

    void onNavigationSubscriptionSelected(NavigationSubscriptionItemModel subscriptionEntity);
}