package com.biniam.rss.expandablerv.listeners;

import com.biniam.rss.expandablerv.ExpandableGroup;

public interface GroupExpandCollapseListener {

    /**
     * Called when a group is expanded
     *
     * @param group the {@link ExpandableGroup} being expanded
     */
    void onGroupExpanded(ExpandableGroup group);

    /**
     * Called when a group is collapsed
     *
     * @param group the {@link ExpandableGroup} being collapsed
     */
    void onGroupCollapsed(ExpandableGroup group);
}
