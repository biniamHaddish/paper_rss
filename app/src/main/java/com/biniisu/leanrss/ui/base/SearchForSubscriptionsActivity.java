package com.biniisu.leanrss.ui.base;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.utils.ReadablyApp;
import com.biniisu.leanrss.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by biniam_Haddish on 12/7/2017.
 * <p>
 * This activity let's a user search for rss feeds using keywords
 */

public class SearchForSubscriptionsActivity extends AppCompatActivity {

    public static final String TAG = SearchForSubscriptionsActivity.class.getSimpleName();

    public static final String FEEDLY_SEARCH_BASE_URL = "https://cloud.feedly.com/";
    //public static final String FEEDLY_SEARCH_BASE_URL = "http://192.168.10.101/~/mock/feedly/";

    public static final String ARTICLE_CONTENT_TYPE = "article";
    public static final String LONG_FORM_CONTENT_TYPE = "longform";
    public static final String FEEDLY_JSON_RESULTS_KEY = "results";
    public static final String LAST_QUERY = "LAST_QUERY";
    public static final String LAST_RESULTS = "LAST_RESULTS";


    private ProgressBar contentLoadingProgressBar;
    private EditText toolbarSearchViewEditText;
    private RelativeLayout resultsTitleContainer;
    private TextView resultCountTextView;
    private RelativeLayout errorMessagesContainer;
    private ImageView errorMessageIcon;
    private TextView errorMessageText;
    private Button retryButton;


    private RecyclerView searchResultList;
    private FeedSearchResultAdapter feedSearchResultAdapter;
    private FeedlySearchService feedlySearchService;
    private List<Call<FeedlySearchResultModel>> searchQueue = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_for_subscriptions);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        contentLoadingProgressBar = findViewById(R.id.searchingProgressBar);

        // Setup the recycler view that will show startSearch results
        searchResultList = findViewById(R.id.feedSearchResultList);

        setUpSearchUI(toolbar);
        setupSearchErrorUI();
        setUpSearchResultRecyclerView();

        feedlySearchService = RetroFitClient.getClient().create(FeedlySearchService.class);
    }

    private void setupSearchErrorUI() {
        resultsTitleContainer = findViewById(R.id.resultTitleContainer);
        resultCountTextView = findViewById(R.id.resultCountTextView);
        errorMessagesContainer = findViewById(R.id.errorMessageContainer);
        errorMessageIcon = findViewById(R.id.errorIcon);
        errorMessageText = findViewById(R.id.errorMessageTextView);
        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(view -> startSearch(toolbarSearchViewEditText.getText().toString()));
    }

    private void setUpSearchResultRecyclerView() {
        feedSearchResultAdapter = new FeedSearchResultAdapter(new ArrayList<>());
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.list_divider_drawable));

        searchResultList.addItemDecoration(dividerItemDecoration);
        searchResultList.setAdapter(feedSearchResultAdapter);

    }

    private void setUpSearchUI(Toolbar toolbar) {
        // Setup the Search container view
        LinearLayout searchContainer = new LinearLayout(this);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        searchContainer.setLayoutParams(containerParams);

        Drawable drawable = getDrawable(R.drawable.ic_arrow_back);
        drawable.setColorFilter(getResources().getColor(R.color.icon_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(drawable);

        // Setup Search view
        toolbarSearchViewEditText = (EditText) LayoutInflater.from(this).inflate(R.layout.search_view_edittext, null);

        // Set width / height / Gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1;
        toolbarSearchViewEditText.setLayoutParams(params);

        toolbarSearchViewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString();
                if (!query.isEmpty()) {
                    startSearch(query);
                }
            }
        });

        toolbarSearchViewEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                toolbarSearchViewEditText.clearFocus();
                startSearch(textView.getText().toString());
                return true;
            }
            return false;
        });

        toolbarSearchViewEditText.requestFocus();

        ImageView clearSearchButton = new ImageView(this);
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearButtonParams.gravity = Gravity.CENTER;
        clearSearchButton.setLayoutParams(clearButtonParams);
        clearSearchButton.setBackground(getDrawable(R.drawable.generic_ripple_drawable));
        clearSearchButton.setImageResource(R.drawable.ic_clear_black_24dp);
        clearSearchButton.setPadding(px, 0, px, 0);
        clearSearchButton.setOnClickListener(view -> toolbarSearchViewEditText.getText().clear());


        searchContainer.addView(toolbarSearchViewEditText);
        searchContainer.addView(clearSearchButton);
        toolbar.addView(searchContainer);
    }

    private void startSearch(String query){

        Call<FeedlySearchResultModel> queue = feedlySearchService.search(query);

        // Cancel all started calls before starting new ones
        for (Call<FeedlySearchResultModel> call : searchQueue) {
            call.cancel();
        }

        showProgressUI();

        searchQueue.add(queue);
        queue.enqueue(new Callback<FeedlySearchResultModel>() {
            @Override
            public void onResponse(@NonNull Call<FeedlySearchResultModel> call, @NonNull retrofit2.Response<FeedlySearchResultModel> response) {
                Log.d(TAG, String.format("onResponse: response string is: %s", response.raw().toString()));
                contentLoadingProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    hideErrorMessageUI();
                    hideProgressUI();

                    List<FeedSearchResultItem> results = response.body().results;

                    if (results == null || results.isEmpty()) {
                        showErrorMessageUI(true, query);
                        return;
                    }

                    List<FeedSearchResultItem> filtered = new ArrayList<>();

                    // We only add that feeds that are of text content to the startSearch result
                    for (FeedSearchResultItem resultItem : results) {
                        if (resultItem.getContentType() != null && (resultItem.getContentType().equals(ARTICLE_CONTENT_TYPE)
                                || resultItem.getContentType().equals(LONG_FORM_CONTENT_TYPE))) {
                            filtered.add(resultItem);
                        }
                        showResultsUI();
                    }

                    feedSearchResultAdapter.setFeedSearchResultItems(filtered);
                    resultCountTextView.setText(String.valueOf(feedSearchResultAdapter.getItemCount()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<FeedlySearchResultModel> call, Throwable t) {
                if (!call.isCanceled()) {
                    t.printStackTrace();
                    showErrorMessageUI(false, query);
                }
                searchQueue.clear();
            }
        });
    }

    private void showResultsUI() {
        searchResultList.setVisibility(View.VISIBLE);
        resultsTitleContainer.setVisibility(View.VISIBLE);
    }

    private void hideResultsUI() {
        searchResultList.setVisibility(View.GONE);
        resultsTitleContainer.setVisibility(View.GONE);

    }

    private void showProgressUI() {
        hideErrorMessageUI();
        hideResultsUI();
        contentLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressUI() {
        contentLoadingProgressBar.setVisibility(View.GONE);
    }

    private void showErrorMessageUI(boolean noResults, String query) {
        hideResultsUI();
        hideProgressUI();
        errorMessagesContainer.setVisibility(View.VISIBLE);
        searchResultList.setVisibility(View.GONE);
        resultsTitleContainer.setVisibility(View.GONE);
        if (noResults) {
            errorMessageIcon.setImageDrawable(getDrawable(R.drawable.ic_search_24px));
            errorMessageText.setText(String.format(getString(R.string.no_search_results), query));
            retryButton.setVisibility(View.GONE);
        } else {
            retryButton.setVisibility(View.VISIBLE);

            errorMessageIcon.setImageDrawable(getDrawable(R.drawable.ic_error_24dp));
            errorMessageText.setText(getText(R.string.search_connection_error));
        }
    }

    private void hideErrorMessageUI() {
        errorMessagesContainer.setVisibility(View.GONE);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // If we had a query let's save it so that we can keep the state
        String query = toolbarSearchViewEditText.getText().toString();
        // If we had results instead of querying again let's just show'em
        List<FeedSearchResultItem> lastResults = feedSearchResultAdapter.getFeedSearchResultItems();

        if (!query.isEmpty()) {
            outState.putString(LAST_QUERY, query);
        }

        if (lastResults != null && lastResults.size() > 0) {
            outState.putSerializable(LAST_RESULTS, (Serializable) lastResults);
        }

        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Keep
    public interface FeedlySearchService {
        @GET("v3/search/feeds")
        Call<FeedlySearchResultModel> search(@Query("q") String query);

        @GET("v3/search/feeds/index.json")
        Call<FeedlySearchResultModel> mockSearch();
    }

    static class RetroFitClient {
        private static Retrofit retrofit = null;

        static Retrofit getClient() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(FEEDLY_SEARCH_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }

    private class FeedSearchResultAdapter extends RecyclerView.Adapter<FeedSearchResultViewHolder> {

        private List<FeedSearchResultItem> feedSearchResultItems;


        FeedSearchResultAdapter(List<FeedSearchResultItem> feedSearchResultItems) {
            this.feedSearchResultItems = feedSearchResultItems;
        }

        @Override
        public FeedSearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FeedSearchResultViewHolder(LayoutInflater.from(SearchForSubscriptionsActivity.this).inflate(R.layout.feed_search_result_item, parent, false));
        }

        @Override
        public void onBindViewHolder(FeedSearchResultViewHolder holder, int position) {
            holder.bindData(feedSearchResultItems.get(position));
        }

        @Override
        public int getItemCount() {
            return feedSearchResultItems.size();
        }

        List<FeedSearchResultItem> getFeedSearchResultItems() {
            return feedSearchResultItems;
        }

        void setFeedSearchResultItems(List<FeedSearchResultItem> feedSearchResultItems) {
            this.feedSearchResultItems = feedSearchResultItems;
            notifyDataSetChanged();
        }
    }

    private class FeedSearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FeedSearchResultItem feedSearchResultItem;
        private ImageView feedResultIcon;
        private TextView feedResultTitle;
        private TextView feedResultDescription;
        private TextView feedResultHost;


        FeedSearchResultViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            feedResultIcon = itemView.findViewById(R.id.feedResultIcon);
            feedResultTitle = itemView.findViewById(R.id.feedResultTitle);
            feedResultDescription = itemView.findViewById(R.id.feedResultDescription);
            feedResultHost = itemView.findViewById(R.id.feedResultHost);

        }


        void bindData(FeedSearchResultItem feedSearchResultItem) {
            this.feedSearchResultItem = feedSearchResultItem;
            feedResultTitle.setText(feedSearchResultItem.getTitle());

            if (feedSearchResultItem.getWebsite() != null) {
                try {
                    URI uri = new URI(feedSearchResultItem.getWebsite());
                    feedResultHost.setText(uri.getHost());

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            if (feedSearchResultItem.getIconUrl() != null) {
                Log.d(TAG, String.format("bindData: iconUrl is %s ", feedSearchResultItem.getIconUrl()));
                feedResultIcon.setClipToOutline(true);
                Glide.with(SearchForSubscriptionsActivity.this).load(feedSearchResultItem.getIconUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                feedResultIcon.setVisibility(View.VISIBLE);
                                feedResultIcon.setImageResource(R.drawable.ic_rss_feed_24px);
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                feedResultIcon.setVisibility(View.VISIBLE);
                                feedResultIcon.setImageDrawable(resource);
                                return true;
                            }
                        }).into(feedResultIcon);
            }


            Log.d(TAG, String.format("bindData: title is %s contentype is %s", feedSearchResultItem.getTitle(), feedSearchResultItem.getContentType()));

            if (feedSearchResultItem.getDescription() == null) {
                feedResultDescription.setText(getString(R.string.no_description));
            } else {
                feedResultDescription.setText(feedSearchResultItem.getDescription());
            }

        }


        @Override
        public void onClick(View view) {
            String subscriptionId = Utils.getSHA1Digest(feedSearchResultItem.getWebsite() + feedSearchResultItem.getFeedId());

            Observable<SubscriptionEntity> subscriptionEntityObservable = new Observable<SubscriptionEntity>() {
                @Override
                protected void subscribeActual(Observer<? super SubscriptionEntity> observer) {
                    observer.onNext(ReadablyApp.getInstance().getDatabase().dao().getSubscription(subscriptionId));
                }
            };

            subscriptionEntityObservable.subscribeOn(Schedulers.io()).subscribeWith(new DisposableObserver<SubscriptionEntity>() {
                @Override
                public void onNext(SubscriptionEntity subscriptionEntity) {
                    if (subscriptionEntity == null) {
                        Intent intent = new Intent(getApplicationContext(), EditSubscriptionActivity.class);
                        intent.putExtra(EditSubscriptionActivity.FEEDLY_SEARCH_RESULT_ITEM_KEY, feedSearchResultItem);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), EditSubscriptionActivity.class);

                        intent.putExtra(EditSubscriptionActivity.SAVED_SUBSCRIPTION_ITEM_KEY, (Serializable) subscriptionEntity);
                        startActivity(intent);
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
    }

    @Keep
    public class FeedSearchResultItem implements Serializable {

        private String feedId;
        private String title;
        private String website;
        private String contentType;
        private String description;
        private String iconUrl;
        private List<String> deliciousTags;


        String getTitle() {
            return title;
        }

        String getDescription() {
            return description;
        }

        String getWebsite() {
            return website;
        }

        String getContentType() {
            return contentType;
        }

        String getFeedId() {
            return feedId.replaceFirst("feed\\/", "");
        }

        String getIconUrl() {
            return iconUrl;
        }

        List<String> getFolders() {
            return deliciousTags;
        }
    }

    @Keep
    class FeedlySearchResultModel {
        @SerializedName(FEEDLY_JSON_RESULTS_KEY)
        private List<FeedSearchResultItem> results;
    }
}
