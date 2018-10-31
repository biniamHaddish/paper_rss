package com.biniam.rss.ui.controllers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biniam.rss.R;
import com.biniam.rss.expandablerv.ExpandableGroup;
import com.biniam.rss.expandablerv.ExpandableRecyclerViewAdapter;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.ui.base.HomeActivity;
import com.biniam.rss.ui.viewmodels.NavigationSubscriptionItemModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NavigationListAdapter extends ExpandableRecyclerViewAdapter<NavigationItemViewHolder, NavigationSubItemViewHolder> {

    public static final String TAG = NavigationListAdapter.class.getSimpleName();
    private NavigationSelectionListener navigationSelectionListener;
    private int selectedSubscriptionPosition;
    private int selectedTagPosition;
    private InternalStatePrefs internalStatePrefs;

    public NavigationListAdapter(List<? extends ExpandableGroup> groups, HomeActivity activity, Set<String> expandedGroups) {
        super(groups, expandedGroups);
        internalStatePrefs = InternalStatePrefs.getInstance(activity.getApplicationContext());
        this.navigationSelectionListener = activity;
    }


    @Override
    public NavigationItemViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item_layout, parent, false);
        Log.d(TAG, "onCreateGroupViewHolder: creating group view holder");
        return new NavigationItemViewHolder(view, this, navigationSelectionListener);
    }

    @Override
    public NavigationSubItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_sub_item_layout, parent, false);
        Log.d(TAG, "onCreateChildViewHolder: creating child view holder");
        return new NavigationSubItemViewHolder(view, this, navigationSelectionListener);
    }

    @Override
    public void onBindChildViewHolder(NavigationSubItemViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final NavigationSubscriptionItemModel navigationSubscriptionItemModel = ((NavigationItem) group).getItems().get(childIndex);
        int unreadCount = ((NavigationItem) group).getSubsUnreadCount().get(childIndex);
        holder.bindSubscription(navigationSubscriptionItemModel, flatPosition == selectedSubscriptionPosition, unreadCount);
    }

    @Override
    public void onBindGroupViewHolder(NavigationItemViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.bind((NavigationItem) group, flatPosition == selectedTagPosition);
    }

    public void setSelectedSubscriptionPosition(int selectedSubscriptionPosition) {
        this.selectedSubscriptionPosition = selectedSubscriptionPosition;
        internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_NAV_POS_PREF_KEY, selectedSubscriptionPosition);
        selectedTagPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedTagPosition(int selectedTagPosition) {
        this.selectedTagPosition = selectedTagPosition;
        internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_NAV_POS_PREF_KEY, selectedTagPosition);
        selectedSubscriptionPosition = -1;
        notifyDataSetChanged();
    }

    public void setAllSubscriptionsSelected() {
        this.selectedSubscriptionPosition = 0;
        internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_NAV_POS_PREF_KEY, 0);
        selectedTagPosition = 0;
        notifyDataSetChanged();
    }

    public void addToExpandedGroups(String expandedGroupName) {
        Set<String> savedExpandedTitles = new HashSet<>(internalStatePrefs.expandedNavTags);
        savedExpandedTitles.add(expandedGroupName);
        internalStatePrefs.setStringSetPrefs(InternalStatePrefs.EXPANDED_NAV_TAGS_PREF_KEY, savedExpandedTitles);
        Log.d(TAG, String.format("addToExpandedGroups: expanded tags are now %s", internalStatePrefs.expandedNavTags));
    }

    public void removeFromExpandedGroups(String collapsedGroupName) {
        Set<String> savedExpandedTitles = new HashSet<>(internalStatePrefs.expandedNavTags);
        savedExpandedTitles.remove(collapsedGroupName);
        internalStatePrefs.setStringSetPrefs(InternalStatePrefs.EXPANDED_NAV_TAGS_PREF_KEY, savedExpandedTitles);
    }
}