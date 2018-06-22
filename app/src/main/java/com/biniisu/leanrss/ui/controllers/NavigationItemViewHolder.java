package com.biniisu.leanrss.ui.controllers;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.expandablerv.viewholders.GroupViewHolder;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.ui.viewmodels.NavigationSubscriptionItemModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class NavigationItemViewHolder extends GroupViewHolder {

    public static final String TAG = NavigationItemViewHolder.class.getSimpleName();

    private TextView navigationItemTitle;
    private ImageView navigationItemIcon;
    private RelativeLayout expandTagRelativeLayout;
    private TextView navigationItemUnreadCount;
    private NavigationSelectionListener navigationSelectionListener;
    private NavigationItem navigationItem;
    private NavigationListAdapter adapter;
    private InternalStatePrefs internalStatePrefs;

    public NavigationItemViewHolder(View itemView, NavigationListAdapter adapter, NavigationSelectionListener navigationSelectionListener) {
        super(itemView);
        navigationItemTitle = itemView.findViewById(R.id.navigationItemTitle);
        navigationItemIcon = itemView.findViewById(R.id.navigationItemIcon);
        navigationItemUnreadCount = itemView.findViewById(R.id.navigationItemUnreadCount);
        expandTagRelativeLayout = itemView.findViewById(R.id.expandTag);

        this.adapter = adapter;
        this.navigationSelectionListener = navigationSelectionListener;

        expandTagRelativeLayout.setOnClickListener(view -> {
            if (!navigationItem.isSubscription()) {
                getListener().onGroupClick(getAdapterPosition());
            }
        });

        internalStatePrefs = InternalStatePrefs.getInstance(itemView.getContext());
    }

    public void bind(NavigationItem navigationItem, boolean selected) {


        this.navigationItem = navigationItem;
        itemView.setActivated(selected);
        Log.d(TAG, String.format("bind: setting tag title to %s", navigationItem.getTitle()));
        navigationItemTitle.setText(navigationItem.getTitle());
        navigationItemIcon.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
        navigationItemIcon.setScaleType(ImageView.ScaleType.CENTER);
        navigationItemUnreadCount.setText(String.valueOf(navigationItem.getUnreadCount()));


        if (navigationItem.isSubscription()) {
            // Make the text style match that of regular subscriptions

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                navigationItemTitle.setTextAppearance(R.style.TextAppearance_AppCompat_Body1); // TODO: find a way to apply text appearance style on api level lower than 23
            }

            navigationItemTitle.setTextSize(16f);

            navigationItemIcon.setImageResource(R.drawable.ic_rss_feed_24px);
            navigationItemIcon.setBackgroundResource(R.drawable.circle_bg);
            navigationItemIcon.setClipToOutline(true);
            navigationItemIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            NavigationSubscriptionItemModel subscriptionEntity = navigationItem.getUntaggedSubscriptionEntity();
            Glide.with(itemView.getContext()).load(subscriptionEntity.iconUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            navigationItemIcon.setImageResource(R.drawable.ic_rss_logo);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            navigationItemIcon.setImageDrawable(resource);
                            return true;
                        }
                    }).into(navigationItemIcon);
        } else if (navigationItem.isAllSubscriptions()) {


            if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD) {
                navigationItemTitle.setText(navigationItemTitle.getContext().getString(R.string.unread));
                navigationItemIcon.setImageResource(R.drawable.ic_nav_unread);
            } else if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
                navigationItemTitle.setText(navigationItemTitle.getContext().getString(R.string.favorites));
                navigationItemIcon.setImageResource(R.drawable.ic_nav_fav);
            } else {
                navigationItemTitle.setText(navigationItemTitle.getContext().getString(R.string.all));
                navigationItemIcon.setImageResource(R.drawable.ic_nav_all_alt);
            }



        }
    }

    @Override
    public void onClick(View v) {
        if (navigationSelectionListener == null) return;

        if (navigationItem.isSubscription()) {
            navigationSelectionListener.onNavigationSubscriptionSelected(navigationItem.getUntaggedSubscriptionEntity());
            adapter.setSelectedSubscriptionPosition(getAdapterPosition());
        } else if (navigationItem.isAllSubscriptions()) {
            navigationSelectionListener.onNavigationEverythingSelected();
            adapter.setAllSubscriptionsSelected();
        } else {
            navigationSelectionListener.onNavigationTagSelected(navigationItem.getTitle());
            adapter.setSelectedTagPosition(getAdapterPosition());
        }


    }

    @Override
    public void expand() {

        if (!navigationItem.isSubscription() && !navigationItem.isAllSubscriptions()) {
            adapter.addToExpandedGroups(navigationItem.getTitle());
            animateExpand();
        }
    }

    @Override
    public void collapse() {
        if (!navigationItem.isSubscription() && !navigationItem.isAllSubscriptions()) {
            adapter.removeFromExpandedGroups(navigationItem.getTitle());
            animateCollapse();
        }
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(0, 90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        navigationItemIcon.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(90, 0, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        navigationItemIcon.setAnimation(rotate);
    }
}