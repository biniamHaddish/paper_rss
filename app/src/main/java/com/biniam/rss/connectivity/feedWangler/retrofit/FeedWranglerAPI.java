package com.biniam.rss.connectivity.feedWangler.retrofit;

import com.biniam.rss.connectivity.feedWangler.FeedWanglerUrls;
import com.biniam.rss.models.feedWragler.FeedCollections;
import com.biniam.rss.models.feedWragler.FeedItemsList;
import com.biniam.rss.models.feedWragler.FeedWranglerAccess;
import com.biniam.rss.models.feedWragler.FeedWranglerFeedIdsList;
import com.biniam.rss.models.feedWragler.FeedWranglerSubscriptions;
import com.biniam.rss.models.feedWragler.NewStreamItem;
import com.biniam.rss.models.feedWragler.StreamItems;
import com.biniam.rss.models.feedWragler.StreamList;
import com.biniam.rss.models.feedWragler.StreamUpdate;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


/**
 * Created by biniam on 12/23/17.
 */

public interface FeedWranglerAPI {

    @GET(FeedWanglerUrls.SUBSCRIPTIONS_LIST)
    Observable<FeedWranglerSubscriptions> getAllSubscriptions();

    /**
     * Login to FeedWrangler
     *
     * @param email
     * @param password
     * @param client_key
     * @return
     */
    @POST(FeedWanglerUrls.AUTHORIZATION_URL)
    Observable<FeedWranglerAccess> login(@Query("email") String email,
                                         @Query("password") String password,
                                         @Query("client_key") String client_key);

    /**
     * Logout from FeedWrangler
     *
     * @return
     */
    @GET(FeedWanglerUrls.LOGOUT_URL)
    Call<JsonObject> logout();

    /**
     * Create FeedWrangler new Subscription
     *
     * @param feed_url
     * @return
     */
    @POST(FeedWanglerUrls.ADD_SUBSCRIPTION)
    Observable<JsonObject> addNewSubscription(@Query("feed_url") String feed_url);

    /**
     * Create new Subscription and wait for the Content to load.
     *
     * @param feed_url
     * @param choose_first
     * @return
     */
    @POST(FeedWanglerUrls.ADD_SUBSCRIPTION_AND_WAIT)
    Observable<JsonObject> subscribeToURLWait(@Query("feed_url") String feed_url, @Query("choose_first") boolean choose_first);

    @POST(FeedWanglerUrls.REMOVE_SUBSCRIPTION)
    Observable<JsonObject> unsubscribeFromFeed(@Query("feed_id") int feed_id);

    @POST(FeedWanglerUrls.RENAME_FEED)
    Observable<JsonObject> renameFeed(@Query("feed_id") int feed_id, @Query("feed_name") String feed_name);

    /*

feed Items

     */

    /**
     * Feed Items are returned from newest to oldest based on the time they were created in the system.
     *
     * @return
     */
    @GET(FeedWanglerUrls.FEED_ITEM_LIST)
    Observable<FeedItemsList> retrieveFeedItems();

    @GET(FeedWanglerUrls.FEED_ITEM_LIST)
    Observable<FeedItemsList> retrieveFeedItem(@Query(value = "read") boolean read,
                                               @Query(value = "limit") int limit);

    /**
     * Will give you only the Feed Ids.
     *
     * @return
     */
    @GET(FeedWanglerUrls.FEED_ITEM_IDS)
    Observable<FeedWranglerFeedIdsList> retrieveFeedItemsIds(@Query(value = "read") boolean read,
                                                             @Query(value = "starred") boolean starred,
                                                             @Query(value = "feed_id") long feed_id,
                                                             @Query(value = "created_since") long created_since,
                                                             @Query(value = "updated_since") long updated_since,
                                                             @Query(value = "limit") int limit);

    //value="group", encoded=true
    @GET(FeedWanglerUrls.FEED_ITEM_IDS)
    Observable<FeedWranglerFeedIdsList> retrieveFeedItemsIds();

    /**
     * comma separated feed item ids
     *
     * @param feed_item_ids
     * @return
     */
    @POST(FeedWanglerUrls.FEED_ITEM_COLLECTIONS)
    Observable<FeedCollections> retrieveFeedItemsCollectionByIds(@Query("feed_item_ids") Object feed_item_ids);

    /**
     * Feed Items are returned from newest to oldest based on the time they were created in the system.
     *
     * @param search_term
     * @return
     */
    @POST(FeedWanglerUrls.FEED_ITEM_SEARCH)
    Observable<FeedCollections> searchFeedItems(@Query("search_term") String search_term);

    /**
     * Marking Multiple Feed Items as read the values for the feed
     *
     * @return
     */
    @POST(FeedWanglerUrls.FEED_ITEMS_MARK_AS_READ)
    Observable<JsonObject> markMultipleFeedItemsAsRead(@Query("feed_item_ids") StringBuilder feed_item_ids);

    @POST(FeedWanglerUrls.FEED_ITEMS_MARK_AS_READ)
    Observable<JsonObject> markFeedItemAsRead(@Query("feed_id") int feed_id);

    /**
     * This method will be responsible for marking single feed item as read or starred or mark it as read for later
     *
     * @param feed_item_id
     * @param read
     * @param starred
     * @param read_later
     * @return
     */
    @POST(FeedWanglerUrls.FEED_ITEM_UPDATE)
    Observable<JsonObject> updateFeedItem(@Query("feed_item_id") int feed_item_id,
                                          @Query("read") boolean read,
                                          @Query("starred") boolean starred,
                                          @Query("read_later") boolean read_later);

    /**
     * Return the IDs of feed_items that have been updated since the given timestamp.
     *
     * @param updated_since
     * @return
     */
    @POST(FeedWanglerUrls.UPDATED_FEED_ITEM_IDS)
    Observable<JsonObject> getUpdatedFeedItemIds(@Query("updated_since") long updated_since);


    /*
    * Streams
    * */

    /**
     * List current Smart Streams
     *
     * @return
     */
    @GET(FeedWanglerUrls.STREAM_LIST)
    Observable<StreamList> listCurrentSmartStreams();

    /**
     * Retrieve the current feed_items in a Smart Stream
     *
     * @param stream_id
     * @return
     */
    @POST(FeedWanglerUrls.STREAM_ITEMS)
    Observable<StreamItems> retrieveCurrentFeedItemsSmartStream(@Query("stream_id") int stream_id);

    /**
     * Retrieve the feed_item_ids of the items in a Smart Stream
     *
     * @param stream_id
     * @return
     */
    @POST(FeedWanglerUrls.STREAM_ITEMS_ID)
    Observable<StreamItems> retrieveFeedItemIdsInSmartStream(@Query("stream_id") int stream_id);

    /**
     * Create new Smart Stream search term is Must be provided for this top work.
     *
     * @param title
     * @param search_term
     * @param all_feeds
     * @param only_unread
     * @return
     */
    @POST(FeedWanglerUrls.CREATE_STREAM)
    Observable<NewStreamItem> createNewSmartStream(@Query("title") String title,
                                                   @Query("search_term") String search_term,
                                                   @Query("all_feeds") boolean all_feeds,
                                                   @Query("only_unread") boolean only_unread);

    /**
     * Update existing Smart Stream
     * IMPORTANT !! Parameters not included in the request are not updated.
     *
     * @param stream_id
     * @param title
     * @return
     */
    @POST(FeedWanglerUrls.UPDATE_STREAM)
    Observable<StreamUpdate> updateSmartStream(@Query("stream_id") int stream_id,
                                               @Query("title") String title,
                                               @Query("search_term") String search_term,
                                               @Query("all_feeds") boolean all_feeds,
                                               @Query("only_unread") boolean only_unread);

    /**
     * Destroy existing Smart Stream or Delete the existing Stream.
     *
     * @param stream_id
     * @return
     */
    @POST(FeedWanglerUrls.DESTROY_STREAM)
    Observable<JsonObject> destroySmartStream(@Query("stream_id") int stream_id);

}
