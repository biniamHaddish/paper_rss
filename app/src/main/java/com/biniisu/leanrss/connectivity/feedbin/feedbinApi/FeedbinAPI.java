package com.biniisu.leanrss.connectivity.feedbin.feedbinApi;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.biniisu.leanrss.models.feedbin.FeedBinEntriesItem;
import com.biniisu.leanrss.models.feedbin.FeedBinSubscriptionsItem;
import com.biniisu.leanrss.models.feedbin.FeedBinTaggingsItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by biniam on 10/24/17.
 */

@Keep
public interface FeedbinAPI {
    /***
     * Get Annotations
     * @return
     */
    @GET("subscriptions.json")
    Observable<List<FeedBinSubscriptionsItem>> getAllFeedbinSubscription();

    @GET("subscriptions/{id}.json")
    Observable<FeedBinSubscriptionsItem> getSubscriptionByID(@Path("id") int subscriptionId);

    @GET("entries.json")
    Observable<List<FeedBinEntriesItem>> getAllFeedEntries();

    @GET("recently_read_entries.json")
    Observable<List<Integer>> getRecentlyReadEntries();

    @GET("entries/{entry_id}/entries.json")
    Observable<FeedBinEntriesItem> getEntryByEntryId(@Path("entry_id") int entry_id);

    @GET("unread_entries.json")
    Observable<List<Integer>> getUnreadEntries();// should be tested thoroughly

    @GET("starred_entries.json")
    Observable<List<Integer>> getStarredEntries(@Query("per_page") int limit);

    @GET("taggings.json")
    Observable<List<FeedBinTaggingsItem>> getAllTages();

    @GET("taggings/{tag_id}.json")
    Observable<FeedBinTaggingsItem> getTageById(@Path("tag_id") int tag_id);

    @GET("recently_read_entries.json")
    Observable<List<Integer>> getRecentlyRead();


    @GET("entries.json")
    Observable<List<FeedBinEntriesItem>> getEntriesForSubscriptionSince(@Query("since") String since,
                                                                        @Query("read") boolean read);

    @GET("entries.json")
    Observable<List<FeedBinEntriesItem>> getEntriesForSubscription(@Query("read") boolean read);

    @GET("entries.json")
    Observable<List<FeedBinEntriesItem>> getEntriesByIds(@Query("ids") String ids);

    @GET("entries.json")
    Observable<List<FeedBinEntriesItem>> getEntriesByIdsWithPage(@Query("ids") String ids, @Query("page") int page);


    @NonNull
    @POST("unread_entries/delete.json")
    Call<JsonArray> markAsRead(@NonNull @Body JsonObject jsonObject);

    @NonNull
    @POST("unread_entries.json")
    Call<JsonArray> markAsUnRead(@NonNull @Body JsonObject jsonObject);

    @NonNull
    @POST("starred_entries.json")
    Call<JsonArray> markAsFavorite(@NonNull @Body JsonObject jsonObject);


    @GET
    Observable<List<FeedBinEntriesItem>> getEntriesPaginatation(@Url String url);

    /**
     * Post Annotations
     *
     * @return
     */
    @NonNull
    @POST("starred_entries.json")
    Call<Void> addStarred(@NonNull @Body JsonObject jsonObject);

    @NonNull
    @POST("unread_entries.json")
    Call<Void> addUnread(@NonNull @Body JsonObject jsonObject);

    @NonNull
    @POST("starred_entries/delete.json")
    Call<Void> removeStarredEntries(@NonNull @Body JsonObject jsonObject);

    @POST("subscriptions.json")
    Call<FeedBinSubscriptionsItem> createSubscription(@NonNull @Body JsonObject feed_url);

    @DELETE("subscriptions/{subscription_id}.json")
    Call<Void> deleteSubscription(@Path("subscription_id") int subscription_id);

    @PATCH("subscriptions/{subscription_id}.json")
    Call<FeedBinSubscriptionsItem> updateSubscriptionTitle(@Path("subscription_id") int subscription_id, @Body JsonObject title);

    @POST("taggings.json")
    Call<Void> createNewTag(@Body JsonObject newTag);

    @DELETE("taggings/{tag_id}.json")
    Call<ResponseBody> deleteTag(@Path("tag_id") int tag_id);

}
