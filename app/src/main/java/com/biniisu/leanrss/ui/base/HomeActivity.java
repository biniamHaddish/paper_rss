package com.biniisu.leanrss.ui.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.persistence.preferences.ReadablyPrefs;
import com.biniisu.leanrss.persistence.preferences.ReadingPrefs;
import com.biniisu.leanrss.ui.controllers.NavigationItem;
import com.biniisu.leanrss.ui.controllers.NavigationListAdapter;
import com.biniisu.leanrss.ui.controllers.NavigationSelectionListener;
import com.biniisu.leanrss.ui.utils.BottomNavigationViewEx;
import com.biniisu.leanrss.ui.utils.SectionedDividerDecoration;
import com.biniisu.leanrss.ui.viewmodels.FeedListItemModel;
import com.biniisu.leanrss.ui.viewmodels.FeedListViewModel;
import com.biniisu.leanrss.ui.viewmodels.NavigationSubscriptionItemModel;
import com.biniisu.leanrss.ui.viewmodels.NavigationTagItemModel;
import com.biniisu.leanrss.utils.AccountBroker;
import com.biniisu.leanrss.utils.AutoSyncManagers.FeedBinSyncJobService;
import com.biniisu.leanrss.utils.ConnectivityState;
import com.biniisu.leanrss.utils.ReadablyApp;
import com.biniisu.leanrss.utils.TemplateExtractor;
import com.biniisu.leanrss.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationSelectionListener {


    public static final String TAG = HomeActivity.class.getSimpleName();

    // Drawer
    private DrawerLayout drawerLayout;

    // Preferences
    private InternalStatePrefs internalStatePrefs;

    // Feed list adapter
    private FeedListAdapter feedListAdapter;

    // Navigation items adapter
    private TextView stickyDateHeaderTextView;

    // View Model for this activity
    private FeedListViewModel feedListViewModel;

    // Room database
    private ReadablyDatabase readablyDatabase;

    private String selectedSubscriptionId;
    private String selectedTagName;
    private boolean allSubscriptionSelected;

    private AccountBroker accountBroker;
    private ReadablyPrefs readablySettings; // App preferences helper
    private ReadingPrefs readingPrefs;
    private RecyclerView feedListRecyclerView;

    private RelativeLayout emptyViewRelativeLayout;
    private ImageView emptyViewImageView;
    private TextView emptyViewTextView;
    private View lastSwipedTopLayer = null;
    private SwipeRefreshLayout swipeRefreshLayout;

    private BroadcastReceiver syncStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView lastSyncTime = findViewById(R.id.lastUpdate);

            switch (intent.getAction()) {
                case AccountBroker.ACTION_SYNC_STARTED:
                    if (lastSyncTime != null) lastSyncTime.setText(getText(R.string.sync_start));
                    break;
                case AccountBroker.ACTION_SYNC_STAGE_SUBSCRIPTIONS:
                    if (lastSyncTime != null)
                        lastSyncTime.setText(getText(R.string.sync_subscriptions));
                    break;
                case AccountBroker.ACTION_SYNC_STAGE_ITEMS:
                    if (lastSyncTime != null) lastSyncTime.setText(getText(R.string.sync_items));
                    break;
                case AccountBroker.ACTION_SYNC_STAGE_IMAGES:
                    if (lastSyncTime != null) lastSyncTime.setText(getText(R.string.cache_images));
                    break;
                case AccountBroker.ACTION_SYNC_FINISHED:
                    if (lastSyncTime != null) {
                        if (internalStatePrefs.lastSyncTime == 0) {
                            lastSyncTime.setText(getString(R.string.not_synced_yet));
                        } else {
                            lastSyncTime.setText(
                                    DateUtils.getRelativeTimeSpanString(internalStatePrefs.lastSyncTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
                        }
                    }
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readingPrefs = ReadingPrefs.getInstance(getApplicationContext());
        accountBroker = AccountBroker.getInstance(getApplicationContext());
        readablySettings = ReadablyPrefs.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());

        // Schedule repeating sync
        if (readablySettings.automaticSync) accountBroker.scheduleAccountJob();

        // Apply selected theme
        applyTheme(false);

        setContentView(R.layout.activity_home_activity);
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
        feedListViewModel = ViewModelProviders.of(this).get(FeedListViewModel.class);

        feedListViewModel.init();
        feedListViewModel.getFeedItemsMutableLiveData().observe(this, new Observer<FeedListItemModel[]>() {
            @Override
            public void onChanged(@Nullable FeedListItemModel[] feedListItemModels) {
                feedListAdapter.swapData(feedListItemModels);
            }
        });

        // Observe changes on tags and subscriptions and showFeedItemsForSubscription the drawer navigation menu accordingly
        feedListViewModel.getTagSubscriptionAggregateMediatorLiveData().observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                updateNavigationList();
            }
        });


        readablyDatabase.dao().getFeedItemsLiveData().observe(this, new Observer<FeedItemEntity[]>() {
            @Override
            public void onChanged(@Nullable FeedItemEntity[] feedItemEntities) {
                //if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD)
                restoreStates();
                updateNavigationList();
                updateTopDateHeader();
            }
        });


        extractHtmlTemplates();
        setUpToolBar();
        setUpViews();
        setUpFeedList();
        restoreStates();

        updateSyncStatus();

        // Register a receiver for sync status
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccountBroker.ACTION_SYNC_STARTED);
        intentFilter.addAction(AccountBroker.ACTION_SYNC_STAGE_SUBSCRIPTIONS);
        intentFilter.addAction(AccountBroker.ACTION_SYNC_STAGE_ITEMS);
        intentFilter.addAction(AccountBroker.ACTION_SYNC_STAGE_IMAGES);
        intentFilter.addAction(AccountBroker.ACTION_SYNC_FINISHED);

        registerReceiver(syncStatusBroadcastReceiver, intentFilter);
    }

    private void setUpToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarTextStyle);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));

        drawerLayout = findViewById(R.id.navigationDrawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            // We are overriding these to disable hamburger animation

            @Override
            public void onDrawerClosed(View drawerView) {
                if (selectedSubscriptionId != null) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, selectedSubscriptionId, internalStatePrefs.sortNewerToOlder);
                } else if (selectedTagName != null) {
                    feedListViewModel.showFeedItemsForTag(internalStatePrefs.selectedFeedFilter, selectedTagName, internalStatePrefs.sortNewerToOlder);
                } else if (allSubscriptionSelected) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder);
                }

                invalidateOptionsMenu();
                updateTopDateHeader();
                unSwipeLastSwipedViewHolder();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateSyncStatus();
            }

        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }


    private void setUpViews() {
        // Setup to launch subscription activity when the plus icon in the drawer is clicked
        RelativeLayout openSettings = findViewById(R.id.openSettings);
        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        ImageView newSubsIcon = findViewById(R.id.newSubIcon);
        TextView settingsLabel = findViewById(R.id.settingsLabel);

        if (accountBroker.isCurrentAccountLocal()) {
            settingsLabel.setVisibility(View.GONE);
            settingsIcon.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, SettingCategoriesActivity.class)));
            openSettings.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, SearchForSubscriptionsActivity.class)));
        } else {
            settingsLabel.setVisibility(View.VISIBLE);
            newSubsIcon.setVisibility(View.GONE);
            openSettings.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, SettingCategoriesActivity.class)));
        }



        // Listen for bottom navigation selection changes
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavView);
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);

        bottomNavigationViewEx.setOnNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            String title = item.getTitle().toString();
            if (title.equals(getString(R.string.unread))) {
                internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_FEED_FILTER_PREF_KEY, InternalStatePrefs.UNREAD);
            } else if (title.equals(getString(R.string.all))) {
                internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_FEED_FILTER_PREF_KEY, InternalStatePrefs.EVERYTHING);
            } else if (title.equals(getString(R.string.favorites))) {
                internalStatePrefs.setIntPref(InternalStatePrefs.SELECTED_FEED_FILTER_PREF_KEY, InternalStatePrefs.FAVORITES);
            }

            if (allSubscriptionSelected) {
                setActionBarTitle(title);
                feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder);
            } else if (selectedSubscriptionId != null) {
                feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, selectedSubscriptionId, internalStatePrefs.sortNewerToOlder);
            } else if (selectedTagName != null) {
                feedListViewModel.showFeedItemsForTag(internalStatePrefs.selectedFeedFilter, selectedTagName, internalStatePrefs.sortNewerToOlder);
            }

            invalidateOptionsMenu();
            updateNavigationList();
            unSwipeLastSwipedViewHolder();
            feedListRecyclerView.scrollToPosition(0);
            return false;
        });


        // Restore bottom navigation last selection
        if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD) {
            bottomNavigationViewEx.getMenu().findItem(R.id.unread).setChecked(true);
        } else if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
            bottomNavigationViewEx.getMenu().findItem(R.id.favorites).setChecked(true);
        } else {
            bottomNavigationViewEx.getMenu().findItem(R.id.all).setChecked(true);
        }


        // Setup swipe to refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (ConnectivityState.hasDataConnection()) {
                // Make the refresh animation disappear after 5 seconds
                startSyncService();
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);
            } else {
                showSnackBarMessage(getString(R.string.no_connection));
                swipeRefreshLayout.setRefreshing(false);
            }

        });

        if (accountBroker.isAccountSyncServiceRunning()) {
            swipeRefreshLayout.setRefreshing(true);
            swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 5000);
        }

        // Date section text
        stickyDateHeaderTextView = findViewById(R.id.sectionDate);
        emptyViewRelativeLayout = findViewById(R.id.emptyView);
        emptyViewImageView = findViewById(R.id.empty_view_icon);
        emptyViewTextView = findViewById(R.id.empty_view_message);

        // Setup account info views
        ImageView accountLogo = findViewById(R.id.accountLogo);
        accountLogo.setImageResource(accountBroker.getAccountLogoRes());

        TextView accountName = findViewById(R.id.accountName);
        accountName.setText(accountBroker.getAccountNameRes());

    }

    private void updateSyncStatus() {
        if (accountBroker.isAccountSyncServiceRunning()) return;

        TextView lastSyncTime = findViewById(R.id.lastUpdate);
        if (internalStatePrefs.lastSyncTime == 0) {
            lastSyncTime.setText(getString(R.string.not_synced_yet));
        } else if ((System.currentTimeMillis() - internalStatePrefs.lastSyncTime) <= 5 * DateUtils.MINUTE_IN_MILLIS) {
            lastSyncTime.setText(String.format(getString(R.string.synced_at), getString(R.string.just_now)));
        } else {
            lastSyncTime.setText(String.format(getString(R.string.synced_at),
                    DateUtils.getRelativeTimeSpanString(internalStatePrefs.lastSyncTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)));
        }
    }

    private void restoreStates() {
        // Restore states

        if (!internalStatePrefs.selectedTagName.isEmpty()) {
            selectedTagName = internalStatePrefs.selectedTagName;
            selectedSubscriptionId = null;
            feedListViewModel.showFeedItemsForTag(internalStatePrefs.selectedFeedFilter, selectedTagName, internalStatePrefs.sortNewerToOlder);
            setActionBarTitle(selectedTagName);
        } else if (!internalStatePrefs.selectedSubscriptionId.isEmpty()) {
            selectedSubscriptionId = internalStatePrefs.selectedSubscriptionId;
            selectedTagName = null;
            feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, selectedSubscriptionId, internalStatePrefs.sortNewerToOlder);

            // Set the subscription title
            new Observable<String>() {
                @Override
                protected void subscribeActual(io.reactivex.Observer<? super String> observer) {
                    SubscriptionEntity subscriptionEntity = readablyDatabase.dao().getSubscription(selectedSubscriptionId);
                    if (subscriptionEntity != null) observer.onNext(subscriptionEntity.title);
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<String>() {
                        @Override
                        public void onNext(String s) {
                            Log.d(TAG, String.format("onCreate: last subscription title was %s", s));
                            setActionBarTitle(s);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } else {
            allSubscriptionSelected = true;
            feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder);
            if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD) {
                setActionBarTitle(getString(R.string.unread));
            } else if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
                setActionBarTitle(getString(R.string.favorites));
            } else {
                setActionBarTitle(getString(R.string.all));
            }

        }
    }

    private void extractHtmlTemplates() {
        TemplateExtractor templatesExtractionHelper = new TemplateExtractor(getApplicationContext());
        templatesExtractionHelper.setTemplateExtractionHelperCallback(new TemplateExtractor.TemplateExtractionHelperCallback() {
            @Override
            public void onTemplateExtractionFinished() {
                internalStatePrefs.setBooleanPref(InternalStatePrefs.TEMPLATES_EXTRACTED_PREF_KEY, true);
            }
        });


        File templatesDir = new File(getExternalFilesDir(null), TemplateExtractor.ASSET_EXTRACTION_DESTINATION);

        if (!internalStatePrefs.templateExtracted ||
                !templatesDir.exists()) {
        }

        templatesExtractionHelper.startExtraction();
    }

    private void updateNavigationList() {

        RecyclerView navigationRecyclerView = findViewById(R.id.navigationList);

        // Setting up navigation subscriptions recycler view
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        new Observable<List<NavigationItem>>() {
            @Override
            protected void subscribeActual(io.reactivex.Observer<? super List<NavigationItem>> observer) {
                List<NavigationTagItemModel> tagNames = readablyDatabase.dao().getNavigationTags();
                List<NavigationItem> tagGroups = new ArrayList<>();

                // Add item to show everything
                int allCount = 0;
                if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
                    allCount = readablyDatabase.dao().getAllSubscriptionsFavCount();
                } else {
                    allCount = readablyDatabase.dao().getAllSubscriptionsUnreadCount();
                }

                NavigationItem everythingNavigationItem = new NavigationItem(new NavigationTagItemModel(getString(R.string.all_subscriptions), allCount), null, null, null);
                everythingNavigationItem.setAllSubscriptions(true);
                tagGroups.add(everythingNavigationItem);
                for (NavigationTagItemModel tag : tagNames) {
                    List<NavigationSubscriptionItemModel> navigationSubscriptions = readablyDatabase.dao().getNavigationSubscriptionsForTag(tag.name);
                    List<NavigationSubscriptionItemModel> filteredSubscriptionItemModels = new ArrayList<>();

                    List<Integer> count = new ArrayList<>();
                    int tagCount = 0;

                    for (NavigationSubscriptionItemModel navigationSubscriptionItemModel : navigationSubscriptions) {

                        int subscriptionCount = 0;

                        if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
                            subscriptionCount = readablyDatabase.dao().getFavCountForSubscription(navigationSubscriptionItemModel.id);
                        } else {
                            subscriptionCount = readablyDatabase.dao().getUnreadCountForSubscription(navigationSubscriptionItemModel.id);
                        }

                        if ((internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD ||
                                internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES)
                                && subscriptionCount == 0) {
                            continue;
                        }

                        tagCount += subscriptionCount;
                        count.add(subscriptionCount);
                        filteredSubscriptionItemModels.add(navigationSubscriptionItemModel);

                    }

                    // Remove tags with zero unread items when unread filter is selected
                    if ((internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD ||
                            internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) && tagCount == 0)
                        continue;

                    tag.tagUnreadCount = tagCount;
                    Log.d(TAG, String.format("subscribeActual: tag %s has %d subscriptions", tag.name, navigationSubscriptions.size()));
                    NavigationItem tagGroup = new NavigationItem(tag, filteredSubscriptionItemModels, count, null);
                    tagGroups.add(tagGroup);
                }

                List<NavigationSubscriptionItemModel> untaggedSubscriptionEntities = readablyDatabase.dao().getUntaggedNavigationSubscriptions();

                for (NavigationSubscriptionItemModel untagged : untaggedSubscriptionEntities) {

                    if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
                        untagged.count = readablyDatabase.dao().getFavCountForSubscription(untagged.id);
                    } else {
                        untagged.count = readablyDatabase.dao().getUnreadCountForSubscription(untagged.id);
                    }

                    // Remove tags with zero unread items when unread filter is selected
                    if ((internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD ||
                            internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) && untagged.count == 0)
                        continue;
                    tagGroups.add(new NavigationItem(new NavigationTagItemModel(untagged.title, untagged.count), null, null, untagged));
                }

                observer.onNext(tagGroups);
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<com.biniisu.leanrss.ui.controllers.NavigationItem>>() {
                    @Override
                    public void onNext(List<com.biniisu.leanrss.ui.controllers.NavigationItem> navigationItems) {

                        NavigationListAdapter listAdapter = new NavigationListAdapter(navigationItems, HomeActivity.this, internalStatePrefs.expandedNavTags);
                        navigationRecyclerView.setAdapter(listAdapter);

                        if (selectedTagName != null) {
                            listAdapter.setSelectedTagPosition(internalStatePrefs.selectedNavPos);
                        } else if (selectedSubscriptionId != null) {
                            listAdapter.setSelectedSubscriptionPosition(internalStatePrefs.selectedNavPos);
                        } else if (allSubscriptionSelected) {
                            listAdapter.setAllSubscriptionsSelected();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void applyTheme(boolean local) {
        if (readablySettings.autoDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
            if (local) getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))
                || readingPrefs.backgroundColor.equals(getString(R.string.onyx))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (local) getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.white))
                || readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (local) getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    private void setUpFeedList() {
        feedListRecyclerView = findViewById(R.id.feedListRecyclerView);
        feedListAdapter = new FeedListAdapter(new FeedListItemModel[]{}, feedListRecyclerView);

        feedListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedListRecyclerView.setAdapter(feedListAdapter);
        SectionedDividerDecoration sectionedDividerDecoration = new SectionedDividerDecoration(this, feedListRecyclerView);
        sectionedDividerDecoration.setDrawable(getDrawable(R.drawable.list_divider_drawable));
        feedListRecyclerView.addItemDecoration(sectionedDividerDecoration);


        feedListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                unSwipeLastSwipedViewHolder();
                if (recyclerView != null && recyclerView.getChildCount() > 2) {
                    View firstVisibleView = recyclerView.getChildAt(0);
                    TextView firstRowTextView = firstVisibleView.findViewById(R.id.listSection);

                    int actual = recyclerView.getChildAdapterPosition(firstVisibleView);
                    boolean isSection = feedListAdapter.isSection(actual);

                    // Reset sticky letter index
                    stickyDateHeaderTextView.setText(firstRowTextView.getText());
                    stickyDateHeaderTextView.setVisibility(View.VISIBLE);

                    if (dy > 0) {
                        if (isSection && actual != 0) {
                            firstRowTextView.setVisibility(View.INVISIBLE);
                        }
                    } else if (dy < 0) {
                        if (isSection && actual != 0) {
                            firstRowTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        Swiper swiper = new Swiper();
        ItemTouchHelper feedListItemTouchHelper = new ItemTouchHelper(swiper);
        feedListItemTouchHelper.attachToRecyclerView(feedListRecyclerView);
    }

    private void updateTopDateHeader() {
        View firstVisibleView = feedListRecyclerView.getChildAt(0);
        if (firstVisibleView == null) return;
        TextView firstRowTextView = firstVisibleView.findViewById(R.id.listSection);
        stickyDateHeaderTextView.setText(firstRowTextView.getText());
        stickyDateHeaderTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD) {
            menu.findItem(R.id.markAllAsRead).setVisible(true);
        } else {
            menu.findItem(R.id.markAllAsRead).setVisible(false);
        }

        if (internalStatePrefs.sortNewerToOlder) {
            menu.findItem(R.id.newToOld).setChecked(true);
        } else {
            menu.findItem(R.id.oldToNew).setChecked(true);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                new Completable() {
                    @Override
                    protected void subscribeActual(CompletableObserver s) {
                        SubscriptionEntity subscriptionEntity = readablyDatabase.dao().getSubscription(selectedSubscriptionId);
                        Intent intent = new Intent(HomeActivity.this, EditSubscriptionActivity.class);
                        intent.putExtra(EditSubscriptionActivity.SAVED_SUBSCRIPTION_ITEM_KEY, (Serializable) subscriptionEntity);
                        startActivity(intent);
                    }
                }.subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Log.d(TAG, "onError: " + e.getMessage());
                            }
                        });
                break;
            case R.id.markAllAsRead:

                MarkAsReadConfirmationDialog markAsReadConfirmationDialog = new MarkAsReadConfirmationDialog();
                Bundle args = new Bundle();

                if (allSubscriptionSelected) {
                    args.putBoolean(MarkAsReadConfirmationDialog.IS_ALL_SUB_EXTRA, true);
                } else if (selectedSubscriptionId != null) {
                    args.putString(MarkAsReadConfirmationDialog.SUB_ID_EXTRA, selectedSubscriptionId);
                    args.putString(MarkAsReadConfirmationDialog.SUB_NAME_EXTRA, getSupportActionBar().getTitle().toString());
                } else if (selectedTagName != null) {
                    args.putString(MarkAsReadConfirmationDialog.TAG_EXTRA, selectedTagName);
                }

                markAsReadConfirmationDialog.setArguments(args);
                markAsReadConfirmationDialog.show(getFragmentManager(), null);
                break;

            case R.id.newToOld:
                internalStatePrefs.setBooleanPref(InternalStatePrefs.SORT_ORDER_NEWER_TO_OLDER_PREF_KEY, true);
                if (selectedSubscriptionId != null) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, selectedSubscriptionId, internalStatePrefs.sortNewerToOlder);
                } else if (selectedTagName != null) {
                    feedListViewModel.showFeedItemsForTag(internalStatePrefs.selectedFeedFilter, selectedTagName, internalStatePrefs.sortNewerToOlder);
                } else if (allSubscriptionSelected) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder);
                }
                feedListRecyclerView.scrollToPosition(0);
                updateTopDateHeader();
                item.setChecked(true);
                break;

            case R.id.oldToNew:
                internalStatePrefs.setBooleanPref(InternalStatePrefs.SORT_ORDER_NEWER_TO_OLDER_PREF_KEY, false);
                if (selectedSubscriptionId != null) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, selectedSubscriptionId, internalStatePrefs.sortNewerToOlder);
                } else if (selectedTagName != null) {
                    feedListViewModel.showFeedItemsForTag(internalStatePrefs.selectedFeedFilter, selectedTagName, internalStatePrefs.sortNewerToOlder);
                } else if (allSubscriptionSelected) {
                    feedListViewModel.showFeedItemsForSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder);
                }

                updateTopDateHeader();
                item.setChecked(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(syncStatusBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme(true);
    }


    private void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void startSyncService() {

        if (accountBroker.isAccountSyncServiceRunning()) {
            Log.d(TAG, "startSyncService: service already running returning");
            return;
        }

        Intent syncServiceIntent = new Intent(HomeActivity.this, accountBroker.getAccountServiceClass());

        if (accountBroker.isCurrentAccountLocal()) {
            List<String> subscriptionIds = new ArrayList<>();

            if (selectedSubscriptionId != null) {
                subscriptionIds.add(selectedSubscriptionId);
                syncServiceIntent.putExtra(FeedBinSyncJobService.SUBSCRIPTION_IDS, (Serializable) subscriptionIds);
                startService(syncServiceIntent);

            } else if (selectedTagName != null) {
                new Observable<List<String>>() {
                    @Override
                    protected void subscribeActual(io.reactivex.Observer<? super List<String>> observer) {
                        observer.onNext(readablyDatabase.dao().getSubscriptionIdsForTag(selectedTagName));
                    }
                }.subscribeOn(Schedulers.io()).subscribeWith(new DisposableObserver<List<String>>() {
                    @Override
                    public void onNext(List<String> strings) {
                        Log.d(TAG, String.format("startSyncService: there %d ids for tag %s", strings.size(), selectedTagName));
                        syncServiceIntent.putExtra(FeedBinSyncJobService.SUBSCRIPTION_IDS, (Serializable) strings);
                        startService(syncServiceIntent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            } else if (allSubscriptionSelected) {
                new Observable<List<String>>() {
                    @Override
                    protected void subscribeActual(io.reactivex.Observer<? super List<String>> observer) {
                        observer.onNext(readablyDatabase.dao().getAllSubscriptionIds());
                    }
                }.subscribeOn(Schedulers.io()).subscribeWith(new DisposableObserver<List<String>>() {
                    @Override
                    public void onNext(List<String> strings) {
                        Log.d(TAG, String.format("startSyncService: there %d ids for tag %s", strings.size(), selectedTagName));
                        syncServiceIntent.putExtra(FeedBinSyncJobService.SUBSCRIPTION_IDS, (Serializable) strings);
                        startService(syncServiceIntent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        } else {
            startService(syncServiceIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }


    private LinkedHashMap<Long, Integer> getSectionDateIndex(FeedListItemModel[] feedEntities) {
        LinkedHashMap<Long, Integer> dateSectionIndex = new LinkedHashMap<>();
        if (feedEntities == null) return dateSectionIndex;

        for (int i = 0; i < feedEntities.length; i++) {
            Long date = feedEntities[i].published;
            Long midNightTime = Utils.getMidNightTimeStamp(date);

            if (!dateSectionIndex.containsKey(midNightTime)) {
                dateSectionIndex.put(midNightTime, i);
            }
        }
        return dateSectionIndex;
    }


    @Override
    public void onNavigationEverythingSelected() {
        if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES) {
            setActionBarTitle(getString(R.string.favorites));
        } else if (internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD) {
            setActionBarTitle(getString(R.string.unread));
        } else {
            setActionBarTitle(getString(R.string.all));
        }

        allSubscriptionSelected = true;
        selectedSubscriptionId = null;
        selectedTagName = null;
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_SUBSCRIPTION_ID_PREF_KEY, "");
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_TAG_NAME_PREF_KEY, "");
        drawerLayout.closeDrawers();
        feedListRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onNavigationTagSelected(String tagName) {
        setActionBarTitle(tagName);
        if (tagName != null && !tagName.equals(selectedTagName))
            feedListRecyclerView.scrollToPosition(0);
        selectedSubscriptionId = null;
        selectedTagName = tagName;
        allSubscriptionSelected = false;
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_SUBSCRIPTION_ID_PREF_KEY, "");
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_TAG_NAME_PREF_KEY, tagName);
        drawerLayout.closeDrawers();
    }

    @Override
    public void onNavigationSubscriptionSelected(NavigationSubscriptionItemModel navigationSubscriptionItemModel) {
        setActionBarTitle(navigationSubscriptionItemModel.title);
        if (selectedSubscriptionId != null && !selectedSubscriptionId.equals(navigationSubscriptionItemModel.id))
            feedListRecyclerView.scrollToPosition(0);
        selectedSubscriptionId = navigationSubscriptionItemModel.id;
        selectedTagName = null;
        allSubscriptionSelected = false;
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_SUBSCRIPTION_ID_PREF_KEY, selectedSubscriptionId);
        internalStatePrefs.setStringPref(InternalStatePrefs.SELECTED_TAG_NAME_PREF_KEY, "");
        drawerLayout.closeDrawers();
    }

    public boolean isOnEverything() {
        return internalStatePrefs.selectedFeedFilter == InternalStatePrefs.EVERYTHING;
    }

    public boolean isOnFavorites() {
        return internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES;
    }

    public boolean isOnUnread() {
        return internalStatePrefs.selectedFeedFilter == InternalStatePrefs.FAVORITES;
    }

    private void showSnackBarMessage(String message) {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.snackBarAnchor);
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void showEmptyView() {
        feedListRecyclerView.setVisibility(View.GONE);
        emptyViewRelativeLayout.setVisibility(View.VISIBLE);
        emptyViewImageView.setVisibility(View.VISIBLE);
        emptyViewTextView.setVisibility(View.VISIBLE);

        new Observable<Boolean>() {
            @Override
            protected void subscribeActual(io.reactivex.Observer<? super Boolean> observer) {
                if (selectedTagName != null) {
                    FeedItemEntity[] feedItemEntities = readablyDatabase.dao().getAllFeedItemsForTag(selectedTagName);
                    observer.onNext(
                            feedItemEntities == null || feedItemEntities.length == 0);
                } else if (selectedSubscriptionId != null) {
                    FeedItemEntity[] feedItemEntities = readablyDatabase.dao().getAllFeedItemsForSubscription(selectedSubscriptionId);
                    observer.onNext(
                            feedItemEntities == null || feedItemEntities.length == 0);
                } else {
                    FeedItemEntity[] feedItemEntities = readablyDatabase.dao().getAllFeedItems();
                    observer.onNext(
                            feedItemEntities == null || feedItemEntities.length == 0);
                }
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean hasNoDataAtAll) {
                        if (hasNoDataAtAll) {
                            emptyViewImageView.setImageResource(R.drawable.ic_sync_black_24dp);
                            emptyViewTextView.setText(getString(R.string.no_sync_yet_message));
                        } else {
                            switch (internalStatePrefs.selectedFeedFilter) {
                                case InternalStatePrefs.FAVORITES:
                                    emptyViewImageView.setImageResource(R.drawable.ic_favorite_feeds_24dp);
                                    if (allSubscriptionSelected) {
                                        emptyViewTextView.setText(getString(R.string.empty_favs_message));
                                    } else {
                                        emptyViewTextView.setText(
                                                String.format(getString(R.string.empty_favs_message_var), getSupportActionBar().getTitle())
                                        );
                                    }
                                    break;
                                case InternalStatePrefs.UNREAD:
                                    emptyViewImageView.setImageResource(R.drawable.ic_hammock_relaxing_);
                                    if (allSubscriptionSelected) {
                                        emptyViewTextView.setText(getString(R.string.all_caught_up_message));
                                    } else {
                                        emptyViewTextView.setText(
                                                String.format(getString(R.string.all_caught_up_message_var), getSupportActionBar().getTitle())
                                        );
                                    }
                                    break;

                                case InternalStatePrefs.EVERYTHING:
                                    emptyViewImageView.setImageResource(R.drawable.ic_assignment_24px);
                                    emptyViewTextView.setText(getString(R.string.no_items_message));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void unSwipeLastSwipedViewHolder() {
        if (lastSwipedTopLayer != null) {
            lastSwipedTopLayer.animate()
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            lastSwipedTopLayer = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            lastSwipedTopLayer = null;
                        }
                    })
                    .translationX(0f).start();
        }
    }

    public class FeedListAdapter extends RecyclerView.Adapter<FeedListViewHolder> {

        private FeedListItemModel[] feedEntities = new FeedListItemModel[]{};
        private LinkedHashMap<Long, Integer> dateSectionIndex;
        private RecyclerView recyclerView;

        FeedListAdapter(FeedListItemModel[] feedEntities, RecyclerView recyclerView) {
            this.feedEntities = feedEntities;
            dateSectionIndex = getSectionDateIndex(feedEntities);
            this.recyclerView = recyclerView;
        }


        @Override
        public FeedListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FeedListViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.feed_list_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(FeedListViewHolder holder, int position) {
            FeedListItemModel feedItemEntity = feedEntities[position];
            Long midNightTime = Utils.getMidNightTimeStamp(feedItemEntity.published);
            holder.bindData(feedEntities[position], getSectionDate(midNightTime), dateSectionIndex.get(midNightTime) == position);
        }

        void swapData(FeedListItemModel[] feedEntities) {

            if (feedEntities == null || feedEntities.length == 0) {
                showEmptyView();
            } else {
                emptyViewRelativeLayout.setVisibility(View.GONE);
                feedListRecyclerView.setVisibility(View.VISIBLE);
            }

            this.feedEntities = feedEntities;
            dateSectionIndex = getSectionDateIndex(feedEntities);
            notifyDataSetChanged();
            recyclerView.invalidateItemDecorations();

            if (feedEntities.length == 0) {
                stickyDateHeaderTextView.setVisibility(View.GONE);
            } else {
                stickyDateHeaderTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return feedEntities == null ? 0 : feedEntities.length;
        }

        String getSectionDate(long time) {

            if (DateUtils.isToday(time)) {
                return getString(R.string.today).toUpperCase();
            }

            return DateUtils.formatDateTime(
                    HomeActivity.this,
                    Utils.getMidNightTimeStamp(time),
                    Utils.isInThisYear(time) ?
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY :
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
            ).toUpperCase();
        }




        boolean isSection(int position) {
            return position >= 0 && dateSectionIndex.containsValue(position);
        }

        public boolean isPreSection(int position) {
            return position >= 0 && dateSectionIndex.containsValue(position + 1);
        }
    }

    public class FeedListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public FeedListItemModel feedItem;
        public TextView sectionTextView;
        private TextView pubDateTextView;
        private TextView subscriptionTitleTextView;
        private TextView feedItemTitle;
        private TextView feedItemExcerpt;
        private ImageView leadImgImageView;
        private ImageView subscriptionIcon;
        private RelativeLayout container;
        private ImageView togglefavStatusItem;
        private ImageView shareItem;
        private ImageView toggleReadStatusItem;

        private View.OnClickListener quickActionsOnClickListener = view -> {
            unSwipeLastSwipedViewHolder();
            switch (view.getId()) {
                case R.id.toggleFav:
                    new Completable() {
                        @Override
                        protected void subscribeActual(CompletableObserver s) {
                            readablyDatabase.dao().
                                    toggleFeedItemFavStatus(feedItem.id, !feedItem.favorite, System.currentTimeMillis());
                        }
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    togglefavStatusItem.setImageResource(feedItem.favorite ?
                                            R.drawable.ic_favorite_feeds_24dp :
                                            R.drawable.ic_favorite_border_black_24dp);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });

                    break;
                case R.id.toggleRead:

                    new Completable() {
                        @Override
                        protected void subscribeActual(CompletableObserver s) {
                            readablyDatabase.dao()
                                    .toggleFeedItemReadStatus(feedItem.id, !feedItem.read, System.currentTimeMillis());

                        }
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    toggleReadStatusItem.setImageResource(feedItem.read ?
                                            R.drawable.ic_read_24dp :
                                            R.drawable.ic_unread_24dp);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });
                    break;
                case R.id.share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, feedItem.link);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_to)));
                    break;
            }

        };

        FeedListViewHolder(View itemView) {
            super(itemView);


            sectionTextView = itemView.findViewById(R.id.listSection);
            pubDateTextView = itemView.findViewById(R.id.feedItemDate);
            subscriptionTitleTextView = itemView.findViewById(R.id.subscriptionTitle);
            feedItemTitle = itemView.findViewById(R.id.feedItemTitle);
            feedItemExcerpt = itemView.findViewById(R.id.feedItemExcerpt);
            leadImgImageView = itemView.findViewById(R.id.leadImg);
            subscriptionIcon = itemView.findViewById(R.id.subscriptionIcon);
            container = itemView.findViewById(R.id.container);

            container.setOnClickListener(this);

            togglefavStatusItem = itemView.findViewById(R.id.toggleFav);
            toggleReadStatusItem = itemView.findViewById(R.id.toggleRead);
            shareItem = itemView.findViewById(R.id.share);


            toggleReadStatusItem.setOnClickListener(quickActionsOnClickListener);
            togglefavStatusItem.setOnClickListener(quickActionsOnClickListener);
            shareItem.setOnClickListener(quickActionsOnClickListener);

            subscriptionIcon.setClipToOutline(true);
        }

        void bindData(FeedListItemModel feedListItemModel, String sectionText, boolean showSection) {
            this.feedItem = feedListItemModel;

            togglefavStatusItem.setImageResource(feedItem.favorite ?
                    R.drawable.ic_favorite_feeds_24dp :
                    R.drawable.ic_favorite_border_black_24dp);

            toggleReadStatusItem.setImageResource(feedItem.read ?
                    R.drawable.ic_read_24dp :
                    R.drawable.ic_unread_24dp);

            feedItemTitle.setText(feedListItemModel.title.trim());

            if (DateUtils.isToday(feedListItemModel.published)) {
                pubDateTextView.setText(DateUtils.getRelativeTimeSpanString(feedListItemModel.published, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
            } else {
                pubDateTextView.setText(DateUtils.formatDateTime(HomeActivity.this, feedListItemModel.published, DateUtils.FORMAT_SHOW_TIME));
            }



            if (getAdapterPosition() == 0) {
                sectionTextView.setVisibility(View.GONE);
            } else {
                sectionTextView.setVisibility(showSection ? View.VISIBLE : View.GONE);
            }

            sectionTextView.setText(sectionText);
            subscriptionTitleTextView.setText(feedListItemModel.subscriptionName);
            feedItemExcerpt.setText(feedListItemModel.excerpt);

            if (isOnEverything() && feedItem.read) {
                applyReadTextColor();
            } else {
                applyRegularColor();
            }

            if (feedListItemModel.leadImgPath != null) {
                leadImgImageView.setVisibility(View.VISIBLE);
                Log.d(TAG, "bindData: loading img into imageview");

                int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                container.measure(spec, spec);
                container.layout(0, 0, container.getMeasuredWidth(), container.getMeasuredHeight());


                if (feedListItemModel.leadImgPath != null) {
                    Glide.with(HomeActivity.this)
                            .load(Uri.parse(feedListItemModel.leadImgPath))
                            .apply(RequestOptions.centerCropTransform()
                                    .dontAnimate()
                                    .override(leadImgImageView.getWidth(), container.getMeasuredHeight()))
                            .into(leadImgImageView);
                } else {
                    leadImgImageView.setImageResource(R.drawable.ic_rss_logo);


                }



            } else {
                leadImgImageView.setVisibility(View.GONE);
            }

            if (feedListItemModel.subscriptionIcon != null) {
                Glide.with(HomeActivity.this)
                        .load(Uri.parse(feedListItemModel.subscriptionIcon))
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_rss_logo))
                        .into(subscriptionIcon);
            } else {
                subscriptionIcon.setImageResource(R.drawable.ic_rss_logo);
            }
        }

        public void applyReadTextColor() {
            feedItemTitle.setTextColor(getResources().getColor(R.color.light_gray_to_storm_dust));
            feedItemExcerpt.setTextColor(getResources().getColor(R.color.light_gray_to_storm_dust));
            subscriptionTitleTextView.setTextColor(getResources().getColor(R.color.light_gray_to_storm_dust));
            pubDateTextView.setTextColor(getResources().getColor(R.color.light_gray_to_storm_dust));
            subscriptionIcon.setColorFilter(getResources().getColor(R.color.bon_jour_trans), PorterDuff.Mode.SRC_ATOP);
        }

        public void applyRegularColor() {
            feedItemTitle.setTextColor(getResources().getColor(R.color.bastille_to_silver_sand));
            feedItemExcerpt.setTextColor(getResources().getColor(R.color.aluminum_to_silver_sand));
            subscriptionTitleTextView.setTextColor(getResources().getColor(R.color.aluminum_to_silver_sand));
            pubDateTextView.setTextColor(getResources().getColor(R.color.aluminum_to_silver_sand));
            subscriptionIcon.setColorFilter(getResources().getColor(R.color.transparent), PorterDuff.Mode.SRC_ATOP);
        }


        @Override
        public void onClick(View view) {
            Intent openFeedItemIntent = new Intent(HomeActivity.this, FeedItemsActivity.class);
            openFeedItemIntent.putExtra(FeedItemsActivity.FEED_ITEM_POS_EXTRA, getAdapterPosition());

            if (selectedSubscriptionId != null) {
                openFeedItemIntent.putExtra(FeedItemsActivity.SUBSCRIPTION_ID_EXTRA, selectedSubscriptionId);
            } else if (selectedTagName != null) {
                openFeedItemIntent.putExtra(FeedItemsActivity.TAG_NAME_EXTRA, selectedTagName);
            } else if (allSubscriptionSelected) {
                openFeedItemIntent.putExtra(FeedItemsActivity.IS_ALL_SUBSCRIPTIONS_EXTRA, true);
            }

            startActivity(openFeedItemIntent);
        }
    }

    class Swiper extends ItemTouchHelper.Callback {

        private boolean swipeBack;


        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.START);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            if (swipeBack) {
                swipeBack = false;
                return 0;
            }
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            View topLayer = ((FeedListViewHolder) viewHolder).container;

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && topLayer != null) {

                unSwipeLastSwipedViewHolder();
                dX = Math.min(dX, -getResources().getDimensionPixelSize(R.dimen.quick_action_item_width) * 3);
                View finalTopLayer = topLayer;

                topLayer.animate()
                        .translationX(dX)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                swipeBack = true;
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                lastSwipedTopLayer = finalTopLayer;

                                finalTopLayer.setOnTouchListener((view, motionEvent) -> {
                                    unSwipeLastSwipedViewHolder();
                                    return false;
                                });
                            }
                        })
                        .start();
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    }


}