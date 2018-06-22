package com.biniam.rss.ui.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biniam.rss.R;
import com.biniam.rss.connectivity.feedbin.feedbinApi.FeedbinAPI;
import com.biniam.rss.connectivity.feedbin.retrofitClient.RetrofitFeedbinClient;
import com.biniam.rss.connectivity.inoreader.InoApiFactory;
import com.biniam.rss.models.feedbin.FeedBinSubscriptionsItem;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.FavIconFetcher;
import com.biniam.rss.utils.ReadablyApp;
import com.biniam.rss.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class EditSubscriptionActivity extends AppCompatActivity {

    public static final String TAG = EditSubscriptionActivity.class.getSimpleName();
    public static final String NO_FEEDLY_RESULT_OBJ_ERR_MSG = "A Feedly search result object or saved subscription need to be provided for this activity";
    public static final String FEEDLY_SEARCH_RESULT_ITEM_KEY = "FEEDLY_SEARCH_RESULT_ITEM_KEY";
    public static final String SAVED_SUBSCRIPTION_ITEM_KEY = "SAVED_SUBSCRIPTION_ITEM_KEY";

    // Feedbin constants
    public static final String FEEDBIN_FEED_URL_JSON_PROPERTY = "feed_url";
    public static final String FEEDBIN_NEW_TAG_FEED_ID_JSON_PROPERTY = "feed_id";
    public static final String FEEDBIN_NEW_TAG_NAME_JSON_PROPERTY = "name";
    public static final String FEEDBIN_UPDATE_SUBSCRIPTION_TITLE_JSON_PROPERTY = "title";

    private SearchForSubscriptionsActivity.FeedSearchResultItem feedSearchResultItem;
    private SubscriptionEntity subscriptionEntity;
    private InoApiFactory inoApiFactory;

    // Views
    private ImageView subscriptionIconImageView;
    private ProgressBar subscriptionIconProgressBar;
    private TextInputEditText subscriptionNameTextInputEditText;
    private TextView folderChooserTitleTextView;
    private TextInputLayout newFolderNameTextInputLayout;
    private TextInputEditText newFolderNameInputEditText;
    private ImageView saveNewFolderImageView;
    private RecyclerView folderList;


    private ReadablyDatabase rssDatabase; // Room database
    private TagChooserAdapter tagChooserAdapter = new TagChooserAdapter(new ArrayList<>());
    private SparseBooleanArray tagSelectionTracker = new SparseBooleanArray();

    private AccountBroker accountBroker;
    private ProgressBar updateProgressBar;
    private MenuItem updateSubscribeMenuItem;
    private CreateNewSubscription createNewSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subscription);
        feedSearchResultItem = (SearchForSubscriptionsActivity.FeedSearchResultItem) getIntent().getSerializableExtra(FEEDLY_SEARCH_RESULT_ITEM_KEY);
        subscriptionEntity = (SubscriptionEntity) getIntent().getSerializableExtra(SAVED_SUBSCRIPTION_ITEM_KEY);
        rssDatabase = ReadablyApp.getInstance().getDatabase();
        accountBroker = AccountBroker.getInstance(getApplicationContext());
        inoApiFactory = InoApiFactory.getInstance(getApplicationContext());

        if (getSupportActionBar() != null) {
            // Customize the action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getDrawable(R.drawable.ic_clear_black_24dp));
            getSupportActionBar().setTitle("");
        }


        if (feedSearchResultItem == null && subscriptionEntity == null)
            throw new IllegalArgumentException(NO_FEEDLY_RESULT_OBJ_ERR_MSG);

        // Map views
        subscriptionIconImageView = findViewById(R.id.subscriptionIcon);
        subscriptionIconProgressBar = findViewById(R.id.subscriptionIconProgress);
        subscriptionNameTextInputEditText = findViewById(R.id.subscriptionName);
        folderChooserTitleTextView = findViewById(R.id.foldersTitle);
        newFolderNameTextInputLayout = findViewById(R.id.newFolderNameContainer);
        newFolderNameInputEditText = findViewById(R.id.folderName);
        saveNewFolderImageView = findViewById(R.id.saveNewFolder);
        folderList = findViewById(R.id.folderChooserList);
        updateProgressBar = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.progress_bar, null, false);

        // Set search result info
        subscriptionNameTextInputEditText.setText(feedSearchResultItem != null ? feedSearchResultItem.getTitle() : subscriptionEntity.title);
        subscriptionNameTextInputEditText.setSelection(subscriptionNameTextInputEditText.getText().length());

        if (accountBroker.isCurrentAccountFeedBin()) {
            subscriptionNameTextInputEditText.setEnabled(false);
        }

        subscriptionIconImageView.setClipToOutline(true);


        // Monitor text changes in new folder creator field
        newFolderNameInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    saveNewFolderImageView.setVisibility(View.VISIBLE);
                } else {
                    saveNewFolderImageView.setVisibility(View.GONE);
                }
            }
        });

        newFolderNameInputEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                newFolderNameInputEditText.clearFocus();
                addNewFolder(textView.getText().toString());
            }
            return true;
        });

        saveNewFolderImageView.setOnClickListener((View view) -> addNewFolder(newFolderNameInputEditText.getText().toString()));

        // Load subscription icon
        Glide.with(getApplicationContext())
                .load(feedSearchResultItem != null ? feedSearchResultItem.getIconUrl() : subscriptionEntity.iconUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        subscriptionIconProgressBar.setVisibility(View.GONE);
                        subscriptionIconImageView.setVisibility(View.VISIBLE);
                        subscriptionIconImageView.setImageResource(R.drawable.ic_rss_feed_24px);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        subscriptionIconProgressBar.setVisibility(View.GONE);
                        subscriptionIconImageView.setVisibility(View.VISIBLE);
                        subscriptionIconImageView.setImageDrawable(resource);
                        return true;
                    }
                }).into(subscriptionIconImageView);

        // Setup folder chooser recycler view
        setUpTagChooser();
    }

    public void setNewsubscription(CreateNewSubscription newsubscription) {
        this.createNewSubscription = newsubscription;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_subscription_menu, menu);
        updateSubscribeMenuItem = menu.findItem(R.id.updateOrSubscribe);

        if (subscriptionEntity != null) {
            updateSubscribeMenuItem.setTitle(getText(R.string.update));
            menu.findItem(R.id.unSubscribe).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.updateOrSubscribe) {
            if (accountBroker.isCurrentAccountLocal()) {
                createOrUpdateLocalSubscription();
            } else if (accountBroker.isCurrentAccountFeedBin()) {
                createOrUpdateFeedbinSubscription();
            } else if (accountBroker.isCurrentAccountInoreader()) {
                createOrUpdateInoreaderSubscription();
            }

        } else if (item.getItemId() == R.id.unSubscribe) {
            Bundle dialogArgs = new Bundle();
            dialogArgs.putSerializable(UnsubscribeDialog.SUBSCRIPTION, subscriptionEntity);
            UnsubscribeDialog unsubscribeDialog = new UnsubscribeDialog();
            unsubscribeDialog.setUnsubscribeListener(new UnsubscribeDialog.UnsubscribeListener() {
                @Override
                public void onUnsubscribed() {

                    if (accountBroker.isCurrentAccountLocal()) {
                        new Observable<Void>() {
                            @Override
                            protected void subscribeActual(Observer<? super Void> observer) {
                                ReadablyApp.getInstance().getDatabase().dao().deleteSubscription(subscriptionEntity);
                                observer.onComplete();
                            }
                        }.subscribeOn(Schedulers.io()).subscribeWith(new DisposableObserver<Void>() {
                            @Override
                            public void onNext(Void aVoid) {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                finish();
                            }
                        });
                    } else if (accountBroker.isCurrentAccountFeedBin()) {

                        updateSubscribeMenuItem.setActionView(updateProgressBar);

                        showSnackBarMessage(getString(R.string.unsubscribing), true);
                        new Completable() {
                            @Override
                            protected void subscribeActual(CompletableObserver s) {
                                try {
                                    Response<Void> response = RetrofitFeedbinClient
                                            .getRetrofit()
                                            .create(FeedbinAPI.class)
                                            .deleteSubscription(Integer.valueOf(subscriptionEntity.id))
                                            .execute();

                                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                                        rssDatabase.dao().deleteSubscription(subscriptionEntity);
                                        s.onComplete();
                                        return;
                                    } else {
                                        Log.w(TAG, String.format("subscribeActual: unsubscribe error code is %d", response.code()));
                                        s.onError(new Exception());
                                    }

                                } catch (IOException e) {
                                    Log.e(TAG, String.format("subscribeActual: error unsubscribing reason : %s", e.getMessage()));
                                    s.onError(e);
                                }
                            }
                        }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        finish();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        invalidateOptionsMenu();
                                        Log.e(TAG, String.format("onError: problem un-subscribing reason: %s", e.getMessage()));
                                        showSnackBarMessage(getString(R.string.err_unsubscribing), false);
                                    }
                                });
                    } else if (accountBroker.isCurrentAccountInoreader()) {

                        updateSubscribeMenuItem.setActionView(updateProgressBar);
                        showSnackBarMessage(getString(R.string.unsubscribing), true);

                        inoApiFactory.editInoSubscription(
                                "unsubscribe",
                                subscriptionEntity.id,
                                null,
                                null,
                                null);
                    }

                }
            });
            unsubscribeDialog.setArguments(dialogArgs);
            unsubscribeDialog.show(getFragmentManager(), null);

        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void createOrUpdateLocalSubscription() {
        new Observable<Void>() {
            @Override
            protected void subscribeActual(Observer<? super Void> observer) {

                if (feedSearchResultItem != null) {

                    // Generate a unique id for this subscription
                    String uniqueId = Utils.getSHA1Digest(feedSearchResultItem.getWebsite() + feedSearchResultItem.getFeedId());

                    // Construct a subscription object
                    SubscriptionEntity subscription = new SubscriptionEntity();
                    subscription.id = uniqueId;
                    subscription.title = subscriptionNameTextInputEditText.getText().toString().trim();
                    subscription.siteLink = feedSearchResultItem.getWebsite();
                    subscription.rssLink = feedSearchResultItem.getFeedId();
                    subscription.createdTimestamp = System.currentTimeMillis();

                    rssDatabase.dao().newSubscription(subscription);

                    // Save selected tags associated with this subscription
                    List<TagEntity> selectedTags = new ArrayList<>();
                    for (int i = 0; i < tagSelectionTracker.size(); i++) {
                        int key = tagSelectionTracker.keyAt(i);
                        TagEntity tagEntity = new TagEntity(uniqueId, tagChooserAdapter.getTagAt(key));
                        selectedTags.add(tagEntity);
                    }

                    rssDatabase.dao().addTags(selectedTags);

                } else {

                    subscriptionEntity.title = subscriptionNameTextInputEditText.getText().toString().trim();

                    if (!subscriptionEntity.title.isEmpty()) {
                        rssDatabase.dao().updateSubscription(subscriptionEntity);
                    }

                    // Update selected tags
                    List<TagEntity> selectedTags = new ArrayList<>();
                    for (int i = 0; i < tagSelectionTracker.size(); i++) {
                        int key = tagSelectionTracker.keyAt(i);
                        TagEntity tagEntity = new TagEntity(subscriptionEntity.id, tagChooserAdapter.getTagAt(key));
                        selectedTags.add(tagEntity);
                    }

                    rssDatabase.dao().addTags(selectedTags);


                    // Delete unchecked tags
                    List<String> allTags = tagChooserAdapter.getTags();
                    List<TagEntity> tagsToBeDeleted = new ArrayList<>();
                    for (int i = 0; i < allTags.size(); i++) {
                        if (!tagSelectionTracker.get(i)) {
                            TagEntity savedTagEntity = rssDatabase.dao().getTag(subscriptionEntity.id, tagChooserAdapter.getTagAt(i));
                            if (savedTagEntity != null) {
                                tagsToBeDeleted.add(savedTagEntity);
                            }
                        }
                    }

                    rssDatabase.dao().deleteTags(tagsToBeDeleted);

                    // If there are no selected tags remove all tags
                    if (selectedTags.isEmpty()) {
                        List<TagEntity> tags = rssDatabase.dao().getSubscriptionTags(subscriptionEntity.id);
                        rssDatabase.dao().deleteTags(tags);
                    }

                }

                // TODO: after subscription is finished open the appropriate section of the app and start syncing that subscription
                observer.onComplete();
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        finish();
                    }
                });
    }

    /**
     * Create new  InoReader subscription
     */
    private void createOrUpdateInoreaderSubscription() {
        updateSubscribeMenuItem.setActionView(updateProgressBar);
        showSnackBarMessage(getString(R.string.updating), true);

        if (feedSearchResultItem != null) {
            Log.d(TAG, "createOrUpdateInoreaderSubscription: " + feedSearchResultItem.getFeedId());
            for (int i = 0; i < tagSelectionTracker.size(); i++) {
                int key = tagSelectionTracker.keyAt(i);
                TagEntity tagEntity = new TagEntity(feedSearchResultItem.getFeedId(), tagChooserAdapter.getTagAt(key));
                inoApiFactory.subscribeFeed(
                        feedSearchResultItem.getTitle(),
                        feedSearchResultItem.getFeedId(),
                        tagEntity.name);
            }

        }
    }

    /**
     * Create new Feedbin Subscription
     */
    private void createOrUpdateFeedbinSubscription() {
        updateSubscribeMenuItem.setActionView(updateProgressBar);
        showSnackBarMessage(getString(R.string.updating), true);
        if (feedSearchResultItem != null) {
            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    JsonObject newSubscriptionJsonObject = new JsonObject();
                    newSubscriptionJsonObject.add(FEEDBIN_FEED_URL_JSON_PROPERTY, new JsonPrimitive(feedSearchResultItem.getFeedId()));

                    // Create new feedbin subscription
                    Response<FeedBinSubscriptionsItem> response = null;
                    int newSubscriptionId = -1;

                    try {
                        response = RetrofitFeedbinClient
                                .getRetrofit()
                                .create(FeedbinAPI.class)
                                .createSubscription(newSubscriptionJsonObject)
                                .execute();

                        if (response.code() == HttpURLConnection.HTTP_CREATED) {
                            FeedBinSubscriptionsItem feedBinSubscriptionsItem = response.body();

                            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
                            subscriptionEntity.id = String.valueOf(feedBinSubscriptionsItem.getFeed_id());
                            subscriptionEntity.title = feedBinSubscriptionsItem.getTitle();
                            subscriptionEntity.siteLink = feedBinSubscriptionsItem.getSite_url();
                            subscriptionEntity.rssLink = feedBinSubscriptionsItem.getFeed_url();
                            subscriptionEntity.iconUrl = FavIconFetcher.getFavIconUrl(subscriptionEntity.siteLink);
                            newSubscriptionId = feedBinSubscriptionsItem.getFeed_id();

                            rssDatabase.dao().newSubscription(subscriptionEntity);
                        }

                    } catch (IOException e) {
                        s.onError(e);
                        s.onComplete();
                        return;
                    }


                    // Save selected tags associated with this subscription
                    for (int i = 0; i < tagSelectionTracker.size(); i++) {
                        int key = tagSelectionTracker.keyAt(i);
                        TagEntity tagEntity = new TagEntity(String.valueOf(newSubscriptionId), tagChooserAdapter.getTagAt(key));

                        JsonObject tagJsonObject = new JsonObject();
                        tagJsonObject.addProperty(FEEDBIN_NEW_TAG_FEED_ID_JSON_PROPERTY, newSubscriptionId);
                        tagJsonObject.addProperty(FEEDBIN_NEW_TAG_NAME_JSON_PROPERTY, tagEntity.name);

                        try {
                            Response<Void> newTagResponse = RetrofitFeedbinClient
                                    .getRetrofit()
                                    .create(FeedbinAPI.class)
                                    .createNewTag(tagJsonObject).execute();

                            if (newTagResponse.code() == HttpURLConnection.HTTP_CREATED) {
                                Log.w(TAG, String.format("subscribeActual: tag \"%s\" created", tagEntity.name));
                            }

                        } catch (IOException e) {
                            Log.e(TAG, String.format("subscribeActual: error creating tag \"%s\"", e.getMessage()));
                            s.onError(e);
                        }

                    }

                }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {

                        boolean errorOccurred;

                        @Override
                        public void onComplete() {
                            if (!errorOccurred) finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            errorOccurred = true;
                            invalidateOptionsMenu();
                            showSnackBarMessage(getString(R.string.conn_err), false);
                        }

                    });

        } else {

            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    // If the title of the subscription has changed lets update it
                    String newTitle = subscriptionNameTextInputEditText.getText().toString().trim();
                    if (!subscriptionEntity.title.equals(newTitle)) {

                        JsonObject newTitleJsonObject = new JsonObject();
                        newTitleJsonObject.addProperty(FEEDBIN_UPDATE_SUBSCRIPTION_TITLE_JSON_PROPERTY, newTitle);

                        try {
                            Log.w(TAG, String.format("subscribeActual: renaming subscription... with data %s", newTitleJsonObject.toString()));
                            Response<FeedBinSubscriptionsItem> updateSubscriptionTitleResponse = RetrofitFeedbinClient
                                    .getRetrofit()
                                    .create(FeedbinAPI.class)
                                    .updateSubscriptionTitle(Integer.valueOf(subscriptionEntity.id), newTitleJsonObject)
                                    .execute();

                            if (updateSubscriptionTitleResponse.code() == HttpURLConnection.HTTP_OK) {
                                Log.w(TAG, String.format("subscribeActual: subscription title has been renamed successfully to %s", newTitle));
                                subscriptionEntity.title = newTitle;
                                rssDatabase.dao().updateSubscription(subscriptionEntity);
                            } else {
                                Log.d(TAG, String.format("subscribeActual: renaming response code %d", updateSubscriptionTitleResponse.code()));
                                s.onError(new Exception("Error"));
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "subscribeActual: there was an error renaming subscription");
                            s.onError(e);
                        }
                    }


                    // Save selected tags associated with this subscription
                    for (int i = 0; i < tagSelectionTracker.size(); i++) {
                        int key = tagSelectionTracker.keyAt(i);
                        TagEntity tagEntity = new TagEntity(subscriptionEntity.id, tagChooserAdapter.getTagAt(key));

                        JsonObject tagJsonObject = new JsonObject();
                        tagJsonObject.addProperty(FEEDBIN_NEW_TAG_FEED_ID_JSON_PROPERTY, subscriptionEntity.id);
                        tagJsonObject.addProperty(FEEDBIN_NEW_TAG_NAME_JSON_PROPERTY, tagEntity.name);

                        if (rssDatabase.dao().getTag(subscriptionEntity.id, tagEntity.name) != null)
                            continue;

                        Log.w(TAG, String.format("subscribeActual: creating tag %s", tagEntity.name));

                        try {
                            Response<Void> newTagResponse = RetrofitFeedbinClient
                                    .getRetrofit()
                                    .create(FeedbinAPI.class)
                                    .createNewTag(tagJsonObject).execute();

                            if (newTagResponse.code() == HttpURLConnection.HTTP_CREATED) {
                                Log.w(TAG, String.format("subscribeActual: tag \"%s\" created", tagEntity.name));
                            } else {
                                new Exception(String.format(": with response code %d", newTagResponse.code()));
                            }

                        } catch (IOException e) {
                            Log.e(TAG, String.format("subscribeActual: error creating tag reason: %s", e.getMessage()));
                            s.onError(e);
                        }

                    }

                    List<String> allTags = tagChooserAdapter.getTags();
                    for (int i = 0; i < allTags.size(); i++) {
                        if (!tagSelectionTracker.get(i)) {
                            TagEntity savedTagEntity = rssDatabase.dao().getTag(subscriptionEntity.id, tagChooserAdapter.getTagAt(i));
                            if (savedTagEntity != null) {

                                Log.w(TAG, String.format("subscribeActual: deleting tag %s", savedTagEntity.name));

                                try {
                                    Response<ResponseBody> deleteTagResponse = RetrofitFeedbinClient
                                            .getRetrofit()
                                            .create(FeedbinAPI.class)
                                            .deleteTag(Integer.valueOf(savedTagEntity.serverId))
                                            .execute();

                                    if (deleteTagResponse.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                                        rssDatabase.dao().deleteTag(savedTagEntity);
                                        Log.w(TAG, String.format("subscribeActual: successfully deleted tag %s", savedTagEntity.name));
                                    } else {
                                        s.onError(new Exception(String.format(": with response code %d", deleteTagResponse.code())));
                                    }

                                } catch (IOException e) {
                                    Log.e(TAG, String.format("subscribeActual: there was an error deleting tag %s", savedTagEntity.name));
                                    s.onError(e);
                                }
                            }
                        }
                    }

                    s.onComplete();


                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(
                            new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, String.format("onError: error occured %s", e.getMessage()));
                                    showSnackBarMessage(getString(R.string.conn_err), false);
                                    invalidateOptionsMenu();
                                }
                            }
                    );

        }
    }

    private void addNewFolder(String folderName) {
        if (folderName.isEmpty()) return;

        new Observable<List<TagEntity>>() {
            @Override
            protected void subscribeActual(Observer<? super List<TagEntity>> observer) {
                observer.onNext(rssDatabase
                        .dao()
                        .getTagByName(folderName.trim()));
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<TagEntity>>() {
                    @Override
                    public void onNext(List<TagEntity> tagEntities) {
                        Log.d(TAG, "onClick: adding new folder");

                        if (!(tagEntities != null && tagEntities.isEmpty())) {
                            newFolderNameTextInputLayout.setError(getText(R.string.tag_already_exists));
                        } else {
                            tagChooserAdapter.addTag(newFolderNameInputEditText.getText().toString());
                            newFolderNameTextInputLayout.setError(null);
                            newFolderNameInputEditText.getText().clear();
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

    /**
     *
     */
    private void setUpTagChooser() {

        folderList.setLayoutManager(new LinearLayoutManager(this));
        folderList.setAdapter(tagChooserAdapter);


        if (feedSearchResultItem != null) {

            new Observable<List<String>>() {
                @Override
                protected void subscribeActual(Observer<? super List<String>> observer) {
                    observer.onNext(rssDatabase.dao().getTagNames());
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<String>>() {
                        @Override
                        public void onNext(List<String> savedTags) {
                            if (savedTags != null && !savedTags.isEmpty() && feedSearchResultItem.getFolders() != null) {

                                folderChooserTitleTextView.setText(getText(R.string.choose_tags));
                                for (int i = 0; i < savedTags.size(); i++) {
                                    Log.d(TAG, String.format("onChanged: searching for %s in suggested tags", savedTags.get(i)));
                                    for (int j = 0; j < feedSearchResultItem.getFolders().size(); j++) {
                                        Log.d(TAG, String.format("onChanged: checking %s", feedSearchResultItem.getFolders().get(j)));
                                        if (savedTags.get(i).equalsIgnoreCase(feedSearchResultItem.getFolders().get(j))) {
                                            Log.d(TAG, String.format("onChanged: found %s in suggested tags at %d", feedSearchResultItem.getFolders().get(j), i));
                                            tagSelectionTracker.put(i, true);
                                            break;
                                        }
                                    }
                                }

                                tagChooserAdapter.setTags(savedTags);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } else {

            // Get all saved tags
            new Observable<List<String>>() {
                @Override
                protected void subscribeActual(Observer<? super List<String>> observer) {
                    observer.onNext(rssDatabase.dao().getTagNames());
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<String>>() {
                        @Override
                        public void onNext(List<String> savedTags) {
                            if (savedTags != null && !savedTags.isEmpty()) {
                                Log.d(TAG, String.format("onNext: we have %d tags", savedTags.size()));
                                tagChooserAdapter.setTags(savedTags);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });


            // Get tags associated with this subscription
            new Observable<List<String>>() {
                @Override
                protected void subscribeActual(Observer<? super List<String>> observer) {
                    observer.onNext(rssDatabase.dao().getSubscriptionTagNames(subscriptionEntity.id));
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<String>>() {
                        @Override
                        public void onNext(List<String> subscriptionTags) {
                            Log.d(TAG, String.format("onNext: %s has %d tag(s)", subscriptionEntity.title, subscriptionTags.size()));
                            tagChooserAdapter.setSelectedTags(subscriptionTags);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void showSnackBarMessage(String message, boolean indefinite) {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.snackBarAnchor);
        Snackbar.make(coordinatorLayout, message, indefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG).show();
    }

    public interface CreateNewSubscription {
        boolean isSubscriptionSuccess(boolean success);
    }

    private class TagChooserAdapter extends RecyclerView.Adapter<TagChooserViewHolder> {

        private List<String> tags;

        TagChooserAdapter(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public TagChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TagChooserViewHolder(
                    LayoutInflater.from(EditSubscriptionActivity.this).inflate(R.layout.tag_chooser_item_layout, parent, false), this);
        }

        @Override
        public void onBindViewHolder(TagChooserViewHolder holder, int position) {
            holder.bind(tags.get(position), tagSelectionTracker.get(position));
        }

        @Override
        public int getItemCount() {
            return tags.size();
        }

        public List<String> getTags() {
            return tags;
        }

        void setTags(List<String> tags) {
            this.tags = tags;
            sort();
            notifyDataSetChanged();
        }

        void setSelectedTags(List<String> selectedTags) {

            for (int i = 0; i < selectedTags.size(); i++) {
                int pos = tags.indexOf(selectedTags.get(i));
                if (pos >= 0) tagSelectionTracker.put(pos, true);
            }

            notifyDataSetChanged();
        }

        String getTagAt(int i) {
            return tags.get(i);
        }

        void addTag(String tag) {
            if (tag != null && !tag.isEmpty()) {
                tags.add(tag);
                tagSelectionTracker.put(tags.indexOf(tag), true);
                notifyDataSetChanged();
            }
        }

        private void sort() {
            Collections.sort(tags, String::compareTo);
        }
    }

    private class TagChooserViewHolder extends RecyclerView.ViewHolder {
        private TextView tagName;
        private CheckBox chooseTag;


        TagChooserViewHolder(View itemView, TagChooserAdapter adapter) {
            super(itemView);

            tagName = itemView.findViewById(R.id.folderName);
            chooseTag = itemView.findViewById(R.id.saveNewFolder);

            View.OnClickListener clickListener = view -> {
                if (chooseTag.isChecked()) {
                    tagSelectionTracker.put(getAdapterPosition(), true);
                } else {
                    tagSelectionTracker.delete(getAdapterPosition());
                }
                adapter.notifyDataSetChanged();
            };

            itemView.setOnClickListener(clickListener);
            chooseTag.setOnClickListener(clickListener);
        }

        void bind(String tagName, boolean chosen) {
            this.tagName.setText(tagName);
            chooseTag.setChecked(chosen);
        }
    }


}
