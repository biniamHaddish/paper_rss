package com.biniisu.leanrss.ui.base;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biniisu.leanrss.BuildConfig;
import com.biniisu.leanrss.R;
import com.biniisu.leanrss.connectivity.inoreader.InoApiFactory;
import com.biniisu.leanrss.leanrssImageViewer.ImageViewerActivity;
import com.biniisu.leanrss.models.MercuryResult;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.persistence.preferences.ReadablyPrefs;
import com.biniisu.leanrss.persistence.preferences.ReadingPrefs;
import com.biniisu.leanrss.readablyYouTubePlayer.PlayYouTubeActivity;
import com.biniisu.leanrss.ui.controllers.FeedItemsPagerAdapter;
import com.biniisu.leanrss.ui.controllers.FeedParser;
import com.biniisu.leanrss.ui.controllers.UiMessagesHandler;
import com.biniisu.leanrss.ui.controllers.WebViewInterceptor;
import com.biniisu.leanrss.ui.utils.BackgroundTasksHandlerThread;
import com.biniisu.leanrss.ui.utils.ImageDownloadCallable;
import com.biniisu.leanrss.ui.utils.ImageDownloadThreadPoolManager;
import com.biniisu.leanrss.ui.utils.ThreadPoolCallback;
import com.biniisu.leanrss.utils.ConnectivityState;
import com.biniisu.leanrss.utils.Constants;
import com.biniisu.leanrss.utils.Utils;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedItemsActivity extends AppCompatActivity implements ThreadPoolCallback, WebViewInterceptor.WebViewInterceptorCallback, LinkOptionsDialogFragment.LinkOptionsDialogCallback, ViewPager.OnPageChangeListener, ReadingAppearanceSettings.ReadingAppearanceCallback, UiMessagesHandler.UiMessagesHandlerCallback {

    public static final String FEED_ITEM_POS_EXTRA = "FEED_ITEM_POS_EXTRA";
    public static final String LAST_UNREAD_FEED_ITEM_EXTRA = "LAST_UNREAD_FEED_ITEM_EXTRA";
    public static final String SUBSCRIPTION_ID_EXTRA = "SUBSCRIPTION_ID_EXTRA";
    public static final String TAG_NAME_EXTRA = "TAG_NAME_EXTRA";
    public static final String IS_ALL_SUBSCRIPTIONS_EXTRA = "IS_ALL_SUBSCRIPTIONS_EXTRA";
    public static final String TAG = FeedItemsActivity.class.getSimpleName();
    private final String MERCURY_PARSER_URL = "https://mercury.postlight.com/parser?url=";
    private final String MERCURY_API_KEY_HEADER = "x-api-key";

    InoApiFactory inoApiFactory;

    private int feedPosition;
    private String subscriptionId;
    private String tagName;
    private boolean isAllSubscriptions;
    private FeedItemsViewPagerFragment feedItemsViewPagerFragment = new FeedItemsViewPagerFragment();

    private FrameLayout root;
    private LinearLayout controlsLinearLayout;
    private ImageView favFeedItemImageView;
    private ImageView setUnreadImageView;
    private ImageView shareImageView;
    private ImageView openInBrowserImageView;
    private ProgressBar feedReadingProgressBar;
    private CardView ratePromptCardView;
    private Button dismissRateCardButton;
    private Button rateReadablyButton;

    private Handler mainThreadHandler = new Handler();
    private boolean controlsVisible = true;
    private boolean isNight;
    private UiMessagesHandler uiMessagesHandler; // The handler for the UI thread. Used for handling messages from worker thread


    // A worker thread which has the same lifecycle with the activity
    // It is created and started in the activity onStart and stopped in activity onStop
    private BackgroundTasksHandlerThread backgroundTasksHandlerThread;

    // A thread pool manager for downloading images
    private ImageDownloadThreadPoolManager imageDownloadThreadPoolManager;
    private CustomTabsIntent customTabsIntent;
    private MenuItem downloadFullArticleMenu;
    private ActionMode actionMode = null;
    private boolean isLandScape;


    private FeedParser feedParser;
    private float lastScrollDistanceY;
    private ReadingAppearanceSettings appearanceSettingsPopupWindow;
    private LinkOptionsDialogFragment linkOptionsDialogFragment = new LinkOptionsDialogFragment();
    private ClipboardManager clipboardManager;
    private FeedItemsPagerAdapter.FeedViewPagerObject currentFeedPagerObject;
    private InternalStatePrefs internalStatePrefs;
    private ReadablyPrefs readablyPrefs;
    private final Runnable showControls = new Runnable() {
        @Override
        public void run() {
            if (!controlsVisible) {
                if (readablyPrefs.fullScreenReading) {
                    feedItemsViewPagerFragment.showControls(isLandScape);
                }
                getSupportActionBar().show();
                controlsVisible = true;
                feedReadingProgressBar.setVisibility(View.VISIBLE);
                controlsLinearLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.snackBarAnchor).setVisibility(View.VISIBLE);
            }
        }
    };
    private final Runnable hideControls = new Runnable() {
        @Override
        public void run() {
            if (controlsVisible) {
                controlsVisible = false;
                feedReadingProgressBar.setVisibility(View.GONE);
                controlsLinearLayout.setVisibility(View.GONE);
                findViewById(R.id.snackBarAnchor).setVisibility(View.GONE);
                if (readablyPrefs.fullScreenReading) {
                    feedItemsViewPagerFragment.hideControls(isLandScape);
                } else {
                    getSupportActionBar().hide();
                }
            }
        }
    };
    private ReadingPrefs readingPrefs;
    private ProgressBar mercuryProgressBar;

    private View.OnClickListener handleBottomControls = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentFeedPagerObject == null) {
                return;
            }
            String url = currentFeedPagerObject.getFeedItem().link;
            switch (view.getId()) {
                case R.id.favoriteItem:
                    toggleCurrentFeedItemFavoriteStatus();
                    break;

                case R.id.markAsUnRead:
                    toggleCurrentFeedItemReadStatus();
                    break;

                case R.id.share:
                    if (URLUtil.isValidUrl(url)) {
                        shareLink(url);
                    }
                    break;

                case R.id.openInBrowser:
                    if (URLUtil.isValidUrl(url)) {
                        customTabsIntent.launchUrl(FeedItemsActivity.this, Uri.parse(url));
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readablyPrefs = ReadablyPrefs.getInstance(getApplicationContext());
        readingPrefs = ReadingPrefs.getInstance(getApplicationContext());
        inoApiFactory = InoApiFactory.getInstance(getApplicationContext());
        // Apply selected theme
        applyTheme(false);
        setContentView(R.layout.activity_feed_item_article_view);
        getSupportActionBar().setElevation(2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        feedParser = FeedParser.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());

        isLandScape = getResources().getBoolean(R.bool.is_landscape);
        isNight = getResources().getBoolean(R.bool.is_night);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        root = findViewById(R.id.articleViewActivity);
        controlsLinearLayout = findViewById(R.id.controlsBar);
        favFeedItemImageView = findViewById(R.id.favoriteItem);
        setUnreadImageView = findViewById(R.id.markAsUnRead);
        shareImageView = findViewById(R.id.share);
        openInBrowserImageView = findViewById(R.id.openInBrowser);
        favFeedItemImageView.setOnClickListener(handleBottomControls);
        shareImageView.setOnClickListener(handleBottomControls);
        setUnreadImageView.setOnClickListener(handleBottomControls);
        openInBrowserImageView.setOnClickListener(handleBottomControls);
        feedReadingProgressBar = findViewById(R.id.readingProgress);
        ratePromptCardView = findViewById(R.id.rateCard);
        dismissRateCardButton = findViewById(R.id.dontRate);
        rateReadablyButton = findViewById(R.id.rateReadably);
        mercuryProgressBar = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.progress_bar, null, false);

        View.OnClickListener rateCardButtonsOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the center for the clipping circle
                int cx = ratePromptCardView.getWidth() / 2;
                int cy = ratePromptCardView.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(ratePromptCardView, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (view.getId() == rateReadablyButton.getId()) {
                            Uri marketUri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(marketUri);
                            startActivity(intent);
                        }
                        ratePromptCardView.setVisibility(View.INVISIBLE);
                    }
                });

                // start the animation
                anim.start();


            }
        };

        dismissRateCardButton.setOnClickListener(rateCardButtonsOnClickListener);
        rateReadablyButton.setOnClickListener(rateCardButtonsOnClickListener);

        // Restore button colors
        updateUIColors();

        Bundle feedFragArgs = new Bundle();
        if (getIntent().hasExtra(SUBSCRIPTION_ID_EXTRA)) {
            subscriptionId = getIntent().getStringExtra(SUBSCRIPTION_ID_EXTRA);
            feedFragArgs.putString(FeedItemsViewPagerFragment.ARG_SUBSCRIPTION_ID, subscriptionId);
        } else if (getIntent().hasExtra(TAG_NAME_EXTRA)) {
            tagName = getIntent().getStringExtra(TAG_NAME_EXTRA);
            feedFragArgs.putString(FeedItemsViewPagerFragment.ARG_TAG_NAME, tagName);
        } else if (getIntent().hasExtra(IS_ALL_SUBSCRIPTIONS_EXTRA)) {
            isAllSubscriptions = getIntent().getBooleanExtra(IS_ALL_SUBSCRIPTIONS_EXTRA, false);
            feedFragArgs.putBoolean(FeedItemsViewPagerFragment.ARG_ALL_SUBSCRIPTIONS, isAllSubscriptions);
        }

        if (getIntent().hasExtra(FEED_ITEM_POS_EXTRA)) {
            feedPosition = getIntent().getIntExtra(FEED_ITEM_POS_EXTRA, 0);
        } else {
            feedPosition = 0;
        }


        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FEED_ITEM_POS_EXTRA))
                feedPosition = savedInstanceState.getInt(FEED_ITEM_POS_EXTRA);

            if (savedInstanceState.containsKey(LAST_UNREAD_FEED_ITEM_EXTRA)) {
                Log.d(TAG, "onCreate: restoring unread item");
                FeedItemEntity lastUnreadFeedItemEntity = (FeedItemEntity) savedInstanceState.getSerializable(LAST_UNREAD_FEED_ITEM_EXTRA);
                feedFragArgs.putSerializable(FeedItemsViewPagerFragment.ARG_LAST_UNREAD_FEED_ITEM, lastUnreadFeedItemEntity);
            }
        }


        // Setup appearance settings popup window
        appearanceSettingsPopupWindow = new ReadingAppearanceSettings(this);
        appearanceSettingsPopupWindow.setReadingAppearanceCallback(this);
        linkOptionsDialogFragment.setLinkOptionsDialogCallback(this);

        // Setup chrome custom tab settings
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.white));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.stack));
        intentBuilder.enableUrlBarHiding();
        intentBuilder.addDefaultShareMenuItem();

        customTabsIntent = intentBuilder.build();
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        feedFragArgs.putInt(FeedItemsViewPagerFragment.ARG_START_POSITION, feedPosition);
        feedItemsViewPagerFragment.setArguments(feedFragArgs);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.feed_items_viewpager_container, feedItemsViewPagerFragment).commit();


        appearanceSettingsPopupWindow.getContentView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                Log.d(TAG, "onViewAttachedToWindow: views attached");
                getSupportActionBar().hide();
                feedReadingProgressBar.setVisibility(View.GONE);
                controlsLinearLayout.setVisibility(View.GONE);
                updateAppearanceSettingsPopupWindowPosition();
                appearanceSettingsPopupWindow.setAutoDarkModeSwitch(readablyPrefs.autoDarkMode);
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                Log.d(TAG, "onViewAttachedToWindow: views detached");
                getSupportActionBar().show();
                feedReadingProgressBar.setVisibility(View.VISIBLE);
                controlsLinearLayout.setVisibility(View.VISIBLE);
                appearanceSettingsPopupWindow.setAutoDarkModeSwitch(readablyPrefs.autoDarkMode);
            }
        });

        root.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visiblity) {
                if ((visiblity & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) {
                    Log.d(TAG, "onSystemUiVisibilityChange: gone full screen");
                } else {
                    mainThreadHandler.post(showControls);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize the handler for the UI thread to handle message from worker threads that download images and full articles
        uiMessagesHandler = new UiMessagesHandler(Looper.getMainLooper(), this);
        // Create and start a new BackgroundTasksHandlerThread worker thread
        backgroundTasksHandlerThread = new BackgroundTasksHandlerThread(BackgroundTasksHandlerThread.NAME);
        backgroundTasksHandlerThread.setFeedReaderUICallbackWeakReference(this);
        backgroundTasksHandlerThread.start();
        // Get the image downloading thread pool manger instance
        imageDownloadThreadPoolManager = ImageDownloadThreadPoolManager.getInstance();
        imageDownloadThreadPoolManager.setFeedReaderUICallbackWeakReference(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Clear the message queue of the BackgroundTasksHandlerThread worker thread stop the current task
        if (backgroundTasksHandlerThread != null) {
            backgroundTasksHandlerThread.quit();
            backgroundTasksHandlerThread.interrupt();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.clear();

        if (currentFeedPagerObject == null) {
            super.onSaveInstanceState(outState);
            return;
        }

        outState.putInt(FEED_ITEM_POS_EXTRA, currentFeedPagerObject.getPosition());

        if (isOnUnread() && currentFeedPagerObject.getFeedItem().read) {
            outState.putSerializable(LAST_UNREAD_FEED_ITEM_EXTRA, currentFeedPagerObject.getFeedItem());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_item_article_view_menu, menu);
        downloadFullArticleMenu = menu.findItem(R.id.downloadFullArticle);

        Drawable appearanceSettingsDrawable = getDrawable(R.drawable.ic_reading_settings);
        Drawable fullArticleDrawable = getDrawable(R.drawable.ic_subject_black_24dp);

        if (readingPrefs.backgroundColor.equals(getString(R.string.white))) {
            Drawable drawable = getDrawable(R.drawable.ic_arrow_back);
            drawable.setColorFilter(getResources().getColor(R.color.mako), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
            appearanceSettingsDrawable.setColorFilter(getResources().getColor(R.color.mako), PorterDuff.Mode.SRC_ATOP);
            fullArticleDrawable.setColorFilter(getResources().getColor(R.color.mako), PorterDuff.Mode.SRC_ATOP);
            mercuryProgressBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.mako)));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            Drawable drawable = getDrawable(R.drawable.ic_arrow_back);
            drawable.setColorFilter(getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
            appearanceSettingsDrawable.setColorFilter(getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);
            fullArticleDrawable.setColorFilter(getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);
            mercuryProgressBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.irish_coffee)));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))) {
            Drawable drawable = getDrawable(R.drawable.ic_arrow_back);
            drawable.setColorFilter(getResources().getColor(R.color.white_var), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
            appearanceSettingsDrawable.setColorFilter(getResources().getColor(R.color.white_var), PorterDuff.Mode.SRC_ATOP);
            fullArticleDrawable.setColorFilter(getResources().getColor(R.color.white_var), PorterDuff.Mode.SRC_ATOP);
            mercuryProgressBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.white_var)));
        } else {
            Drawable drawable = getDrawable(R.drawable.ic_arrow_back);
            drawable.setColorFilter(getResources().getColor(R.color.jumbo), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
            appearanceSettingsDrawable.setColorFilter(getResources().getColor(R.color.jumbo), PorterDuff.Mode.SRC_ATOP);
            fullArticleDrawable.setColorFilter(getResources().getColor(R.color.jumbo), PorterDuff.Mode.SRC_ATOP);
            mercuryProgressBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.jumbo)));
        }

        downloadFullArticleMenu.setIcon(fullArticleDrawable);
        menu.findItem(R.id.showAppearanceSettings).setIcon(appearanceSettingsDrawable);

        if (currentFeedPagerObject != null && currentFeedPagerObject.getFeedItem().hasFullArticle())
            downloadFullArticleMenu.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onActionModeStarted(ActionMode mode) {
        if (actionMode == null) {
            actionMode = mode;
            Menu menu = mode.getMenu();
        }
        super.onActionModeStarted(mode);
    }


    public void toggleCurrentFeedItemFavoriteStatus() {
        FeedItemEntity feedItemEntity = currentFeedPagerObject.getFeedItem();
        if (feedItemEntity.favorite) {
            feedItemEntity.favorite = false;
            favFeedItemImageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        } else {
            feedItemEntity.favorite = true;
            favFeedItemImageView.setImageResource(R.drawable.ic_favorite_feeds_24dp);
        }

        Log.d(TAG, "toggleCurrentFeedItemFavoriteStatus");
        currentFeedPagerObject.updateFeedItem(true);
    }

    public void toggleCurrentFeedItemReadStatus() {
        FeedItemEntity genericFeedItem = currentFeedPagerObject.getFeedItem();

        if (genericFeedItem.read) {
            genericFeedItem.read = false;
            setUnreadImageView.setImageResource(R.drawable.ic_unread_24dp);
        } else {
            genericFeedItem.read = true;
            setUnreadImageView.setImageResource(R.drawable.ic_read_24dp);
        }
        currentFeedPagerObject.updateFeedItem(true);
    }


    public void updateReadingProgress() {
        float progressPercent = (((float) currentFeedPagerObject.getPosition() + 1f) / (float) feedItemsViewPagerFragment.getFeedItemsCount()) * 100f;
        feedReadingProgressBar.setProgress((int) progressPercent);
    }


    public void setFavFeedItemImageView(boolean fav) {
        if (fav) {
            favFeedItemImageView.setImageResource(R.drawable.ic_favorite_feeds_24dp);
        } else {
            favFeedItemImageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }


    private void updateUIColors() {


        if (readablyPrefs.autoDarkMode) {
            if (isNight) {
                Log.d(TAG, "updateUIColors: changing to night colors");
                if (readablyPrefs.nightReadingBgColor.equals(getString(R.string.gray_bg_name))) {
                    readingPrefs.backgroundColor = getString(R.string.scarpa_flow);
                    readingPrefs.textColor = getString(R.string.scarpa_flow_bg_text_color);
                    readingPrefs.linkColor = getString(R.string.scarpa_flow_bg_link_color);

                    readingPrefs.setStringPref(ReadingPrefs.BACKGROUND_COLOR_PREF_KEY, getString(R.string.scarpa_flow));
                    readingPrefs.setStringPref(ReadingPrefs.LINK_COLOR_PREF_KEY, getString(R.string.scarpa_flow_bg_link_color));
                    readingPrefs.setStringPref(ReadingPrefs.TEXT_COLOR_PREF_KEY, getString(R.string.scarpa_flow_bg_text_color));


                } else if (readablyPrefs.nightReadingBgColor.equals(getString(R.string.black_bg_name))) {
                    readingPrefs.backgroundColor = getString(R.string.onyx);
                    readingPrefs.textColor = getString(R.string.onyx_bg_text_color);
                    readingPrefs.linkColor = getString(R.string.onyx_bg_link_color);

                    readingPrefs.setStringPref(ReadingPrefs.BACKGROUND_COLOR_PREF_KEY, getString(R.string.onyx));
                    readingPrefs.setStringPref(ReadingPrefs.LINK_COLOR_PREF_KEY, getString(R.string.onyx_bg_link_color));
                    readingPrefs.setStringPref(ReadingPrefs.TEXT_COLOR_PREF_KEY, getString(R.string.onyx_bg_text_color));
                }
            } else {
                Log.d(TAG, "updateUIColors: changing to day colors");
                if (readablyPrefs.dayReadingBgColor.equals(getString(R.string.sepia_bg_name))) {
                    readingPrefs.backgroundColor = getString(R.string.merino);
                    readingPrefs.textColor = getString(R.string.merino_bg_text_color);
                    readingPrefs.linkColor = getString(R.string.merino_bg_link_color);

                    readingPrefs.setStringPref(ReadingPrefs.BACKGROUND_COLOR_PREF_KEY, getString(R.string.merino));
                    readingPrefs.setStringPref(ReadingPrefs.LINK_COLOR_PREF_KEY, getString(R.string.merino_bg_link_color));
                    readingPrefs.setStringPref(ReadingPrefs.TEXT_COLOR_PREF_KEY, getString(R.string.merino_bg_text_color));
                } else if (readablyPrefs.dayReadingBgColor.equals(getString(R.string.white_bg_name))) {
                    readingPrefs.backgroundColor = getString(R.string.white);
                    readingPrefs.textColor = getString(R.string.white_bg_text_color);
                    readingPrefs.linkColor = getString(R.string.white_bg_link_color);

                    readingPrefs.setStringPref(ReadingPrefs.BACKGROUND_COLOR_PREF_KEY, getString(R.string.white));
                    readingPrefs.setStringPref(ReadingPrefs.LINK_COLOR_PREF_KEY, getString(R.string.white_bg_link_color));
                    readingPrefs.setStringPref(ReadingPrefs.TEXT_COLOR_PREF_KEY, getString(R.string.white_bg_text_color));
                }
            }
        }


        if (readingPrefs.backgroundColor.equals(getString(R.string.white))) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.silver_sand));
            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white)); // Change the background color of the actionbar
            setOverflowColor(getResources().getColor(R.color.mako)); // Change overflow icon color

            controlsLinearLayout.setBackgroundColor(getResources().getColor(R.color.white));

            // Change the reading progress color
            feedReadingProgressBar.setBackgroundColor(getResources().getColor(R.color.white_var));
            feedReadingProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.mako)));

            favFeedItemImageView.setColorFilter(getResources().getColor(R.color.mako));
            setUnreadImageView.setColorFilter(getResources().getColor(R.color.mako));
            shareImageView.setColorFilter(getResources().getColor(R.color.mako));
            openInBrowserImageView.setColorFilter(getResources().getColor(R.color.mako));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.irish_coffee));
            setOverflowColor(getResources().getColor(R.color.irish_coffee));
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.merino));

            controlsLinearLayout.setBackgroundColor(getResources().getColor(R.color.merino));
            // Change the reading progress color
            feedReadingProgressBar.setBackgroundColor(getResources().getColor(R.color.merino));
            feedReadingProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.bison_hide)));

            favFeedItemImageView.setColorFilter(getResources().getColor(R.color.irish_coffee));
            setUnreadImageView.setColorFilter(getResources().getColor(R.color.irish_coffee));
            shareImageView.setColorFilter(getResources().getColor(R.color.irish_coffee));
            openInBrowserImageView.setColorFilter(getResources().getColor(R.color.irish_coffee));

        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.tuatara));

            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.scarpa_flow));
            setOverflowColor(getResources().getColor(R.color.white_var));
            controlsLinearLayout.setBackgroundColor(getResources().getColor(R.color.scarpa_flow));

            // Change the reading progress color
            feedReadingProgressBar.setBackgroundColor(getResources().getColor(R.color.jumbo));
            feedReadingProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.white_var)));

            favFeedItemImageView.setColorFilter(getResources().getColor(R.color.white_var));
            setUnreadImageView.setColorFilter(getResources().getColor(R.color.white_var));
            shareImageView.setColorFilter(getResources().getColor(R.color.white_var));
            openInBrowserImageView.setColorFilter(getResources().getColor(R.color.white_var));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));

            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.tuatara));
            setOverflowColor(getResources().getColor(R.color.jumbo));
            controlsLinearLayout.setBackgroundColor(getResources().getColor(R.color.tuatara));

            // Change the reading progress color
            feedReadingProgressBar.setBackgroundColor(getResources().getColor(R.color.cod_gray));
            feedReadingProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.jumbo)));

            feedReadingProgressBar.setBackgroundColor(getResources().getColor(R.color.cod_gray));

            favFeedItemImageView.setColorFilter(getResources().getColor(R.color.jumbo));
            setUnreadImageView.setColorFilter(getResources().getColor(R.color.jumbo));
            shareImageView.setColorFilter(getResources().getColor(R.color.jumbo));
            openInBrowserImageView.setColorFilter(getResources().getColor(R.color.jumbo));
        }

        if (feedItemsViewPagerFragment.isAdded()) feedItemsViewPagerFragment.setViewPagerBg();
        invalidateOptionsMenu();
        rebuildCustomTabs();
    }


    public void downloadImagesForCurrentPage() {
        FeedItemsPagerAdapter.FeedViewPagerObject feedPagerObject = feedItemsViewPagerFragment.getCurrentPagerObject();
        Document currentPageDOM = feedPagerObject.getPageDOM();
        Elements images = currentPageDOM.getElementsByTag(FeedParser.IMG_TAG);

        Log.d(TAG, String.format("downloadImagesForCurrentPage: there are %d images here", images.size()));

        // Let's first prepare the cache folder that images will be downloaded
        String subscriptionId = feedPagerObject.getFeedItem().subscriptionId;
        String feedItemId = feedPagerObject.getFeedItem().id;

        // Create the subscription directory
        File subscriptionDir = new File(getCacheDir(), subscriptionId);

        if (!subscriptionDir.exists()) subscriptionDir.mkdir();

        // Create the feed item directory as sub-directory of subscriptionDir
        File feedItemDir = new File(subscriptionDir, feedItemId);

        if (!feedItemDir.exists()) feedItemDir.mkdir();
        for (int i = 0; i < images.size(); i++) {
            Element img = images.get(i);
            String domId = img.attr(FeedParser.ID_ATTR);
            String url = img.attr(FeedParser.ORG_SRC_ATTR);

            if (!Boolean.parseBoolean(img.attr(FeedParser.IMG_DOWNLAODED))) {
                ImageDownloadCallable imageDownloadCallable = new ImageDownloadCallable(feedItemDir, currentFeedPagerObject.getFeedItem().id, domId, url, currentFeedPagerObject.getFeedItem().leadImgPath == null && i == 0);
                imageDownloadCallable.setImageDownloadThreadPoolManger(imageDownloadThreadPoolManager);
                imageDownloadThreadPoolManager.addCallable(imageDownloadCallable);
            }
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        actionMode = null;
        super.onActionModeFinished(mode);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.favFeed:
//                Log.d(TAG, "onContextItemSelected: clicked test menu");
//                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (readablyPrefs.switchUsingVolButton) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                feedItemsViewPagerFragment.goToNextFeed();
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                feedItemsViewPagerFragment.goToPreviousFeed();
            }

            return true;
        }

        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) &&
                readablyPrefs.switchUsingVolButton) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.downloadFullArticle:
                if (currentFeedPagerObject != null && !currentFeedPagerObject.getFeedItem().hasFullArticle()) {
                    if (ConnectivityState.hasDataConnection()) {
                        // Cancel all image downloads, if any
                        downloadFullArticleMenu.setActionView(mercuryProgressBar);
                        imageDownloadThreadPoolManager.cancelAllTask();
                        runMercuryParser(currentFeedPagerObject);
                    } else {
                        if (controlsVisible)
                            showSnackBar(R.string.no_connection, false, null, null);
                    }
                }
                break;
            case R.id.showAppearanceSettings:
                int width = getResources().getDimensionPixelSize(R.dimen.font_chooser_width);
                int x = Utils.getDeviceMetrics(this).widthPixels - (width + getResources().getDimensionPixelSize(R.dimen.regular_padding));
                int y = ((getSupportActionBar().getHeight() / 2) + getResources().getDimensionPixelSize(R.dimen.regular_padding));
                appearanceSettingsPopupWindow.showAtLocation(findViewById(R.id.articleViewActivity), Gravity.NO_GRAVITY, x, y);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateAppearanceSettingsPopupWindowPosition() {
        int width = getResources().getDimensionPixelSize(R.dimen.font_chooser_width);
        int x = Utils.getDeviceMetrics(this).widthPixels - (width + getResources().getDimensionPixelSize(R.dimen.regular_padding));
        int y = ((getSupportActionBar().getHeight() / 2) + getResources().getDimensionPixelSize(R.dimen.regular_padding));

        appearanceSettingsPopupWindow.update(x, y, width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showSnackBar(int messageRes, boolean indefintie, String action, View.OnClickListener actionOnClickListener) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackBarAnchor), messageRes, indefintie ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);

        int snackBarTextViewId = android.support.design.R.id.snackbar_text;
        TextView snackTextView = snackbar.getView().findViewById(snackBarTextViewId);
        snackbar.getView().setElevation(2f);

        if (readingPrefs.backgroundColor.equals(getString(R.string.white))) {
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.silver_sand));
            if (snackTextView != null)
                snackTextView.setTextColor(getResources().getColor(R.color.white));
            snackbar.setActionTextColor(getResources().getColor(R.color.white));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.irish_coffee));
            if (snackTextView != null)
                snackTextView.setTextColor(getResources().getColor(R.color.white));
            snackbar.setActionTextColor(getResources().getColor(R.color.white));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))) {
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.tuatara));
            if (snackTextView != null)
                snackTextView.setTextColor(getResources().getColor(R.color.white_var));
            snackbar.setActionTextColor(getResources().getColor(R.color.white_var));
        } else {
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.tuatara));
            if (snackTextView != null)
                snackTextView.setTextColor(getResources().getColor(R.color.white_var));
            snackbar.setActionTextColor(getResources().getColor(R.color.white_var));
        }
        if (action != null && actionOnClickListener != null)
            snackbar.setAction(action, actionOnClickListener);
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        imageDownloadThreadPoolManager.cancelAllTask();
        feedItemsViewPagerFragment.removeAllViews();
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


    @Override
    public void onBackPressed() {
        if (appearanceSettingsPopupWindow.isShowing()) {
            Log.d(TAG, "onBackPressed: backpressed");
            appearanceSettingsPopupWindow.dismiss();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: configuration changed");
        isLandScape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        updateAppearanceSettingsPopupWindowPosition();
        if (isLandScape) {
            feedItemsViewPagerFragment.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

    }

    private void setImage(final WebView webView, final String id, final String imageSrc) {
        Log.d(TAG, String.format("setImage: setting image to %s", imageSrc));
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(
                        "setImage(\'#" + id + "\'," + "\'" + imageSrc + "\')", null
                );
            }
        });
    }



    public void updateCurrentPage() {

        final FeedItemsPagerAdapter.FeedViewPagerObject feedViewPagerObject = feedItemsViewPagerFragment.getCurrentPagerObject();
        setUnreadImageView.setImageResource(R.drawable.ic_read_black_24dp);
        backgroundTasksHandlerThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                feedViewPagerObject.getFeedItem().read = true;
                feedViewPagerObject.updateFeedItem(true);
            }
        });

        setFavFeedItemImageView(feedViewPagerObject.getFeedItem().favorite);
        updateReadingProgress();
        if (ConnectivityState.isOnWiFi()) {
            // Before starting new image download batch cancel existing threads
            imageDownloadThreadPoolManager.cancelAllTask();
            // If we are on Wi-Fi download all the images in this page automatically
            downloadImagesForCurrentPage();
        }
    }


    private void shareLink(String url) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_to)));
    }

    @Override
    public void publish(Message message) {
        // add the message from worker thread to UI thread's message queue
        if (uiMessagesHandler != null) uiMessagesHandler.sendMessage(message);
    }

    @Override
    public void showLinkOptionsDialog(String url) {
        if (URLUtil.isValidUrl(url)) {
            Bundle args = new Bundle();
            args.putString(LinkOptionsDialogFragment.LINK_URL, url);
            linkOptionsDialogFragment.setArguments(args);
            linkOptionsDialogFragment.show(getSupportFragmentManager(), url);
        }
    }

    @Override
    public void openUrl(String url) {
        if (!URLUtil.isValidUrl(url)) return;

        // try to determine if the link clicked is actual link of the feed item it it's try to run it through mercury parser
        String feedLink = currentFeedPagerObject.getFeedItem().link;
        if (feedLink.equals(url) || feedLink.contains(url)) {
            mainThreadHandler.post(showControls);

            if (ConnectivityState.hasDataConnection()) {
                // Cancel all ongoing image downloads, if any
                mainThreadHandler.post(() -> downloadFullArticleMenu.setActionView(mercuryProgressBar));
                imageDownloadThreadPoolManager.cancelAllTask();
                runMercuryParser(currentFeedPagerObject);
            } else {
                if (controlsVisible) showSnackBar(R.string.no_connection, false, null, null);
            }
        } else {
            mainThreadHandler.post(() -> customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(url)));

        }
    }

    @Override
    public void openYoutubeVideo(String id, boolean isList) {
        if (ConnectivityState.hasDataConnection()) {
            Intent intent = new Intent(this, PlayYouTubeActivity.class);
            if (isList) {
                intent.putExtra(PlayYouTubeActivity.LIST_ID_EXTRA, id);
            } else {
                intent.putExtra(PlayYouTubeActivity.VIDEO_ID_EXTRA, id);
            }
            startActivity(intent);
        } else {
            showSnackBar(R.string.no_connection, false, null, null);
        }

    }

    @Override
    public void downloadImage(String domId, String url, String placeholderSrc) {
        openDownloadedImage(domId);
    }

    @Override
    public void openDownloadedImage(String domId) {


        Elements images = currentFeedPagerObject.getPageDOM().getElementsByTag(FeedParser.IMG_TAG);

        String currentPath = currentFeedPagerObject.getPageDOM().getElementById(domId).attr(FeedParser.SRC_ATTR);
        List<String> imgPaths = new ArrayList<>();


        for (Element img : images) {

        }

        for (Element img : images) {

            if (Boolean.valueOf(img.attr(FeedParser.IMG_DOWNLAODED))) {
                imgPaths.add(img.attr(FeedParser.SRC_ATTR));
            }
            Log.d(TAG, String.format("openDownloadedImage: adding image %s", img.attr(FeedParser.SRC_ATTR)));

//            if (img.id().equals(domId)){
//                position = img.siblingIndex();
//            }
        }

        int position = imgPaths.lastIndexOf(currentPath);

        Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.IMG_START_POS, position);
        intent.putExtra(ImageViewerActivity.IMG_ELEMENTS, (Serializable) imgPaths);

        Log.d(TAG, String.format("openDownloadedImage: opening image with an id of %s and has %d images", domId, images.size()));
        startActivity(intent);
    }

    @Override
    public void onWebViewSingleTapped() {
        Log.d(TAG, "onWebViewSingleTapped called!");
        // PopupWindows in Android 5.0 and 5.1 doesn't get dismissed with click outside of the popup window
        // so we dismisse them when the webview is tapped instead
        if (appearanceSettingsPopupWindow.isShowing() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            appearanceSettingsPopupWindow.dismiss();
        }

        if (controlsVisible) {
            mainThreadHandler.post(hideControls);
        } else {
            mainThreadHandler.post(showControls);
        }
    }

    @Override
    public void onWebViewDoubleTapped() {
        if (currentFeedPagerObject != null && !currentFeedPagerObject.getFeedItem().hasFullArticle() && readablyPrefs.doubleTapForFullArticle) {
            if (ConnectivityState.hasDataConnection()) {
                // Cancel all ongoing image downloads, if any
                downloadFullArticleMenu.setActionView(mercuryProgressBar);
                imageDownloadThreadPoolManager.cancelAllTask();
                runMercuryParser(currentFeedPagerObject);
            } else {
                if (controlsVisible) showSnackBar(R.string.no_connection, false, null, null);
            }
        }
    }

    @Override
    public void onWebViewScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.d(TAG, String.format("onWebViewScrolled: x -> %f, y -> %f", distanceX, distanceY));

        float diffY = lastScrollDistanceY - distanceY;
        boolean inThreshold = Math.abs(diffY) >= 10;

        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            Log.d(TAG, String.format("onWebViewScrolled: distance x -> %f is greater than distance y - > %f", distanceX, distanceY));
            return;
        }

        if (inThreshold) {
            lastScrollDistanceY = distanceY;
            if (distanceY < 0) {
                mainThreadHandler.post(showControls);
            } else {
                mainThreadHandler.post(hideControls);
            }
        }
    }

    @Override
    public void viewPagerFistPageLoaded() {
        currentFeedPagerObject = feedItemsViewPagerFragment.getCurrentPagerObject();
        updateCurrentPage();
    }

    @Override
    public void openLinkClicked(String url) {
        mainThreadHandler.post(() -> customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(url)));

    }

    @Override
    public void shareLinkClicked(String url) {
        shareLink(url);
    }

    @Override
    public void copyLinkClicked(String url) {
        ClipData.Item linkItem = new ClipData.Item(url);
        ClipDescription linkClipDescription = new ClipDescription(" ", new String[]{"text/plain"});
        ClipData linkClipData = new ClipData(linkClipDescription, linkItem);
        clipboardManager.setPrimaryClip(linkClipData);
        if (controlsVisible) {
            Snackbar.make(findViewById(R.id.snackBarAnchor), R.string.link_copied_message, Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int position) {

        // Show rate prompt if the user has read 100 articles
        if (!internalStatePrefs.ratePromptShown && internalStatePrefs.readArticlesCount >= Constants.READ_RATE_PROMPT_ARTICLE_COUNT) {
            // Show rate prompt with circular animation
            internalStatePrefs.setBooleanPref(InternalStatePrefs.RATE_PROMPT_SHOWN_PREF_KEY, true);

            // get the center for the clipping circle
            int cx = ratePromptCardView.getWidth() / 2;
            int cy = ratePromptCardView.getHeight() / 2;

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(ratePromptCardView, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            ratePromptCardView.setVisibility(View.VISIBLE);
            anim.start();
        } else if (!internalStatePrefs.ratePromptShown) {
            internalStatePrefs.setIntPref(InternalStatePrefs.READ_ARTICLES_COUNT_PREF_KEY, internalStatePrefs.readArticlesCount + 1);
        }

        Log.d(TAG, String.format("onPageSelected: ratePromptShown %b, readArticlesCount %d", internalStatePrefs.ratePromptShown, internalStatePrefs.readArticlesCount));

        currentFeedPagerObject = feedItemsViewPagerFragment.getCurrentPagerObject();
        updateCurrentPage();
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onJustificationChanged(final String justification) {
        currentFeedPagerObject = feedItemsViewPagerFragment.getCurrentPagerObject();
        feedItemsViewPagerFragment.updateCSS();
        SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> feedViewPagerObjectSparseArray = feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray();
        for (int i = 0; i < feedViewPagerObjectSparseArray.size(); i++) {
            final WebView webView = feedViewPagerObjectSparseArray.get(feedViewPagerObjectSparseArray.keyAt(i)).getWebView();
            webView.evaluateJavascript(
                    "updateJustification('" + justification + "')"
                    , null);
        }
    }

    @Override
    public void onLineHeightChanged(final float lineHeight) {
        feedItemsViewPagerFragment.updateCSS();
        SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> feedViewPagerObjectSparseArray = feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray();
        for (int i = 0; i < feedViewPagerObjectSparseArray.size(); i++) {
            final WebView webView = feedViewPagerObjectSparseArray.get(feedViewPagerObjectSparseArray.keyAt(i)).getWebView();
            webView.evaluateJavascript(
                    "updateLineHeight('" + String.valueOf(lineHeight) + "')"
                    , null);
        }
    }

    @Override
    public void onFontSizeChanged(final int titleFontSize, final int contentFontSize, final int articleInfoFontSize) {
        feedItemsViewPagerFragment.updateCSS();
        SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> feedViewPagerObjectSparseArray = feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray();
        for (int i = 0; i < feedViewPagerObjectSparseArray.size(); i++) {
            final WebView webView = feedViewPagerObjectSparseArray.get(feedViewPagerObjectSparseArray.keyAt(i)).getWebView();
            Log.d(TAG, "onFontSizeChanged: font size changed");
            webView.evaluateJavascript(
                    "updateFontSize("
                            + "'" + String.valueOf(titleFontSize) + "',"
                            + "'" + String.valueOf(contentFontSize) + "',"
                            + "'" + String.valueOf(articleInfoFontSize) + "')"
                    , null);
        }
    }

    @Override
    public void onAutoDarkModeSwitchClicked(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                    && !readablyPrefs.autoDarkMode) {
                // Request for location permission
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ReadingPreferencesFragment.MY_PERMISSIONS_REQUEST_ACCESS_LOCATION
                );
                return;
            }
        }

        readablyPrefs.updateBooleanPref(getString(R.string.pref_auto_dark_mode_title), enabled);

        if (readablyPrefs.autoDarkMode) {
            updateUIColors();

            if (isNight) {
                if (readablyPrefs.nightReadingBgColor.equals(getString(R.string.gray_bg_name))) {
                    onBackgroundColorChanged(getString(R.string.scarpa_flow),
                            getString(R.string.scarpa_flow_bg_text_color),
                            getString(R.string.scarpa_flow_bg_link_color),
                            false,
                            R.color.scarpa_flow);

                } else if (readablyPrefs.nightReadingBgColor.equals(getString(R.string.black_bg_name))) {

                    onBackgroundColorChanged(getString(R.string.onyx),
                            getString(R.string.onyx_bg_text_color),
                            getString(R.string.onyx_bg_link_color),
                            false,
                            R.color.onyx);
                }
            } else {

                if (readablyPrefs.dayReadingBgColor.equals(getString(R.string.sepia_bg_name))) {

                    onBackgroundColorChanged(getString(R.string.merino),
                            getString(R.string.merino_bg_text_color),
                            getString(R.string.merino_bg_link_color),
                            false,
                            R.color.merino);

                } else if (readablyPrefs.dayReadingBgColor.equals(getString(R.string.white_bg_name))) {

                    onBackgroundColorChanged(getString(R.string.white),
                            getString(R.string.white_bg_text_color),
                            getString(R.string.white_bg_link_color),
                            false,
                            R.color.white);

                }
            }
        }
    }

    @Override
    public void onBackgroundColorChanged(final String bgColor, final String textColor, final String linkColor, boolean isNight, int colorRes) {
        feedItemsViewPagerFragment.updateCSS();
        SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> feedViewPagerObjectSparseArray = feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray();
        for (int i = 0; i < feedViewPagerObjectSparseArray.size(); i++) {
            int j = feedViewPagerObjectSparseArray.keyAt(i);
            if (feedViewPagerObjectSparseArray.get(j) != null) {
                final WebView webView = feedViewPagerObjectSparseArray.get(j).getWebView();
                assert webView != null;
                webView.post(() -> webView.evaluateJavascript(
                        "setBackgroundColor("
                                + "\'" + bgColor + "\'" + ","
                                + "\'" + textColor + "\'" + ","
                                + "\'" + linkColor + "\'"
                                + ")"
                        , null));
            }
        }

        updateUIColors();

    }

    @Override
    public void onFontChosen(final String fontName) {
        feedItemsViewPagerFragment.updateCSS();
        SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> feedViewPagerObjectSparseArray = feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray();
        for (int i = 0; i < feedViewPagerObjectSparseArray.size(); i++) {
            int j = feedViewPagerObjectSparseArray.keyAt(i);
            if (feedViewPagerObjectSparseArray.get(j) != null) {
                final WebView webView = feedViewPagerObjectSparseArray.get(j).getWebView();
                assert webView != null;
                webView.evaluateJavascript(
                        "updateFont('" + fontName + "')"
                        , null);
            }
        }
    }

    @Override
    public void onImageDownloadSuccessful(String feedItemId, String imgDomId, String downloadedFileUri) {
        if (feedItemId.equals(currentFeedPagerObject.getFeedItem().id)) {
            // Only updateFeedListDateSections image using js if the source feed item id and current page id are the same
            setImage(currentFeedPagerObject.getWebView(), imgDomId, downloadedFileUri);
        }

        if (currentFeedPagerObject.getPageDOM().getElementById(imgDomId) == null) return;

        // Update the DOM
        currentFeedPagerObject
                .getPageDOM()
                .getElementById(imgDomId)
                .attr(FeedParser.IMG_DOWNLAODED, "true");

        currentFeedPagerObject
                .getPageDOM()
                .getElementById(imgDomId)
                .attr(FeedParser.SRC_ATTR, downloadedFileUri);

        currentFeedPagerObject.getFeedItem().leadImgPath = downloadedFileUri;

        currentFeedPagerObject.persistDOM();

    }

    @Override
    public void onImageDownloadError(String imageDomId) {
        // Nothing to do yet
    }


    public boolean isOnUnread() {
        return internalStatePrefs.selectedFeedFilter == InternalStatePrefs.UNREAD;
    }

    private void applyTheme(boolean local) {
        if (readablyPrefs.autoDarkMode) {
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

    private void setOverflowColor(int color) {
        final String overflowDesc = getString(android.support.v7.appcompat.R.string.abc_action_menu_overflow_description);
        // The top-level window
        final ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decor.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decor.findViewsWithText(outViews, overflowDesc,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

                if (outViews.isEmpty()) {
                    Log.d(TAG, String.format("setOverflowColor: overflow imgview is null %s", overflowDesc));
                    return;
                }

                AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                overflow.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        });
    }

    private void rebuildCustomTabs() {
        if (readingPrefs.backgroundColor.equals(getString(R.string.white))) {
            // Setup chrome custom tab settings
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            intentBuilder.setToolbarColor(getResources().getColor(R.color.white));
            intentBuilder.setSecondaryToolbarColor(getResources().getColor(R.color.silver_sand));
            intentBuilder.enableUrlBarHiding();
            intentBuilder.addDefaultShareMenuItem();

            customTabsIntent = intentBuilder.build();
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            // Setup chrome custom tab settings
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            intentBuilder.setToolbarColor(getResources().getColor(R.color.merino));
            intentBuilder.setSecondaryToolbarColor(getResources().getColor(R.color.irish_coffee));
            intentBuilder.enableUrlBarHiding();

            intentBuilder.addDefaultShareMenuItem();

            customTabsIntent = intentBuilder.build();

        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))) {
            // Setup chrome custom tab settings
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.scarpa_flow));
            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.tuatara));
            intentBuilder.enableUrlBarHiding();
            intentBuilder.addDefaultShareMenuItem();

            customTabsIntent = intentBuilder.build();
        } else {
            // Setup chrome custom tab settings
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.onyx));
            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.black));
            intentBuilder.enableUrlBarHiding();
            intentBuilder.addDefaultShareMenuItem();

            customTabsIntent = intentBuilder.build();
        }
    }

    private void runMercuryParser(final FeedItemsPagerAdapter.FeedViewPagerObject feedViewPagerObject) {
        new Observable<FeedItemsPagerAdapter.FeedViewPagerObject>() {
            @Override
            protected void subscribeActual(Observer<? super FeedItemsPagerAdapter.FeedViewPagerObject> observer) {
                try {
                    showSnackBar(R.string.loading_mercury_parser, true, null, null);
                    FeedItemEntity feedItemEntity = currentFeedPagerObject.getFeedItem();

                    Request request = new Request.Builder()
                            .url(MERCURY_PARSER_URL + feedItemEntity.link)
                            .addHeader(MERCURY_API_KEY_HEADER, Constants.MERCURY_API_KEY)
                            .build();

                    // Request for a full article version of urlToParse
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Gson gson = new Gson();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseJSON = response.body().string();
                    MercuryResult mercuryResult = gson.fromJson(responseJSON, MercuryResult.class);

                    if (mercuryResult != null) {
                        if (mercuryResult.getContent() != null
                                && !mercuryResult.getContent().isEmpty()) {
                            Document contentDOM = Jsoup.parse(mercuryResult.getContent());
                            Elements imgs = contentDOM.getElementsByTag(FeedParser.IMG_TAG);

                            if (URLUtil.isValidUrl(mercuryResult.getLead_image_url()) && imgs.isEmpty()) {
                                // Only append lead image into content if the content has no images at all
                                // If MercuryResult contains leading images let's make it part of the content
                                contentDOM.prependElement(FeedParser.IMG_TAG).attr(FeedParser.SRC_ATTR, mercuryResult.getLead_image_url());
                                feedItemEntity.fullArticle = contentDOM.toString();
                            } else {
                                feedItemEntity.fullArticle = mercuryResult.getContent();
                            }

                            if (mercuryResult.getAuthor() != null && !mercuryResult.getAuthor().trim().isEmpty()) {
                                feedItemEntity.author = mercuryResult.getAuthor();
                            }

                            feedViewPagerObject.setFeedItem(feedParser.parseFeedItem(feedItemEntity));
                            observer.onNext(feedViewPagerObject);
                        }
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }

                observer.onComplete();
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<FeedItemsPagerAdapter.FeedViewPagerObject>() {

                    @Override
                    public void onNext(FeedItemsPagerAdapter.FeedViewPagerObject feedViewPagerObject) {
                        if (feedItemsViewPagerFragment.getFeedViewPagerObjectSparseArray().get(feedViewPagerObject.getPosition()) != null) {
                            feedViewPagerObject.getWebView().reload();
                            if (currentFeedPagerObject.getPosition() == feedViewPagerObject.getPosition()) {
                                showSnackBar(R.string.mercury_parser_loaded, false, null, null);
                                invalidateOptionsMenu();
                                downloadImagesForCurrentPage();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (feedViewPagerObject.getPosition() == currentFeedPagerObject.getPosition()) {
                            showSnackBar(R.string.mercury_parser_failure, false, getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (ConnectivityState.hasDataConnection()) {
                                        // Cancel all image downloads, if any
                                        downloadFullArticleMenu.setActionView(mercuryProgressBar);
                                        imageDownloadThreadPoolManager.cancelAllTask();
                                        runMercuryParser(currentFeedPagerObject);
                                    } else {
                                        if (controlsVisible)
                                            showSnackBar(R.string.no_connection, false, null, null);
                                    }
                                }
                            });
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ReadingPreferencesFragment.MY_PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: location permission granted");

                readablyPrefs.updateBooleanPref(getString(R.string.pref_auto_dark_mode_title), true);

            } else {
                Log.d(TAG, "onRequestPermissionsResult: location permission denied");

                readablyPrefs.updateBooleanPref(getString(R.string.pref_auto_dark_mode_title), false);
            }
        }
    }
}