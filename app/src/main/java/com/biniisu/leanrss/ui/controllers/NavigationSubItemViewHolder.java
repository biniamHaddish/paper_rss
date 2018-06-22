package com.biniisu.leanrss.ui.controllers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.expandablerv.viewholders.ChildViewHolder;
import com.biniisu.leanrss.ui.viewmodels.NavigationSubscriptionItemModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class NavigationSubItemViewHolder extends ChildViewHolder {

    private ImageView icon;
    private TextView subscriptionName;
    private TextView unReadCount;
    private NavigationSubscriptionItemModel navigationSubscriptionItemModel;
    private NavigationSelectionListener navigationSelectionListener;
    private NavigationListAdapter adapter;

    public NavigationSubItemViewHolder(View itemView, NavigationListAdapter adapter, NavigationSelectionListener navigationSelectionListener) {
        super(itemView);
        this.navigationSelectionListener = navigationSelectionListener;
        this.adapter = adapter;
        icon = itemView.findViewById(R.id.subscriptionIcon);
        subscriptionName = itemView.findViewById(R.id.subscriptionName);
        unReadCount = itemView.findViewById(R.id.subscriptionUnreadCount);

        itemView.setOnClickListener(view -> {
            if (navigationSelectionListener != null && navigationSubscriptionItemModel != null) {
                navigationSelectionListener.onNavigationSubscriptionSelected(navigationSubscriptionItemModel);
                adapter.setSelectedSubscriptionPosition(getAdapterPosition());
            }
        });
    }


    public void bindSubscription(NavigationSubscriptionItemModel navigationSubscriptionItemModel, boolean selected, int unreadCount) {
        this.navigationSubscriptionItemModel = navigationSubscriptionItemModel;
        itemView.setActivated(selected);
        subscriptionName.setText(navigationSubscriptionItemModel.title);
        unReadCount.setText(String.valueOf(unreadCount));
        icon.setClipToOutline(true);
        Glide.with(itemView.getContext()).load(navigationSubscriptionItemModel.iconUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        icon.setImageResource(R.drawable.ic_rss_logo);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        icon.setImageDrawable(resource);
                        return true;
                    }
                }).into(icon);
    }


}