package com.biniam.rss.persistence.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Keep;

import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.ui.viewmodels.FeedListItemModel;
import com.biniam.rss.ui.viewmodels.NavigationSubscriptionItemModel;
import com.biniam.rss.ui.viewmodels.NavigationTagItemModel;

import java.util.List;

/**
 * Created by biniam_Haddish on 11/27/17.
 * <p>
 * Room DAO for local feeds
 */

@Keep
@Dao
public interface ReadablyDAO {


    /**
     * {@link FeedItemEntity} operations
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeedItems(List<FeedItemEntity> feedItems);

    @Update
    void updateFeedItem(FeedItemEntity feedItem);

    @Update
    void updateFeedItems(FeedItemEntity[] feedItemEntities);

    @Delete
    void deleteFeedItems(List<FeedItemEntity> feedItems);

    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE id = :guid")
    FeedItemEntity getFeedItem(String guid);

    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE id IN (:ids)")
    FeedItemEntity[] getFeedItemsForIds(List<Integer> ids);

    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE)
    LiveData<FeedItemEntity[]> getFeedItemsLiveData();

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " ORDER by published DESC")
    FeedListItemModel[] getAllFeedListModels();

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " ORDER by published")
    FeedListItemModel[] getAllFeedListModelsOlderToNewer();

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " ORDER by published DESC")
    FeedItemEntity[] getAllFeedItems();

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " ORDER by published")
    FeedItemEntity[] getAllFeedItemsOlderToNewer();

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE read = 0 ORDER by published DESC")
    FeedListItemModel[] getAllUnreadFeedListModels();

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE read = 0 ORDER by published")
    FeedListItemModel[] getAllUnreadFeedListModelsOlderToNewer();

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE read = 0 ORDER by published DESC")
    FeedItemEntity[] getAllUnreadFeedItems();

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE read = 0 ORDER by published")
    FeedItemEntity[] getAllUnreadFeedItemsOlderToNewer();


    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE read = 1 ORDER by published DESC")
    FeedItemEntity[] getAllReadFeedItems();



    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE favorite = 1 ORDER by published DESC")
    FeedListItemModel[] getAllFavoriteFeedListModels();

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE favorite = 1 ORDER by published")
    FeedListItemModel[] getAllFavoriteFeedListModelsOlderToNewer();

    @Query("SELECT * " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 ORDER by published DESC")
    FeedItemEntity[] getAllFavoriteFeedListItems();

    @Query("SELECT * " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 ORDER by published")
    FeedItemEntity[] getAllFavoriteFeedListItemsOlderToNewer();

    @Query("SELECT id " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 ORDER by published DESC")
    int[] getFavItemsIds();

    @Query("SELECT id " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 ORDER by published DESC")
    List<String> getFavItemsIdsAsList();

    @Query("SELECT id " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE subscriptionId = :subscriptionId")
    int[] getFeedItemIdsForSubscription(String subscriptionId);


    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE subscriptionId = :subscriptionId ORDER by published DESC")
    FeedListItemModel[] getAllFeedItemsForSubscriptionFeedListModels(String subscriptionId);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE subscriptionId = :subscriptionId ORDER by published")
    FeedListItemModel[] getAllFeedItemsForSubscriptionFeedListModelsOlderToNewer(String subscriptionId);

    @Query("SELECT * " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE subscriptionId = :subscriptionId ORDER by published DESC")
    FeedItemEntity[] getAllFeedItemsForSubscription(String subscriptionId);

    @Query("SELECT * " +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE subscriptionId = :subscriptionId ORDER by published")
    FeedItemEntity[] getAllFeedItemsForSubscriptionOlderToNewer(String subscriptionId);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE read = 0 and subscriptionId = :subscriptionId ORDER by published DESC")
    FeedListItemModel[] getUnreadItemsForSubscriptionFeedListModels(String subscriptionId);


    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE read = 0 and subscriptionId = :subscriptionId ORDER by published")
    FeedListItemModel[] getUnreadItemsForSubscriptionFeedListModelsOlderToNewer(String subscriptionId);



    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE read = 0 and subscriptionId = :subscriptionId ORDER by published DESC")
    FeedItemEntity[] getUnreadItemsForSubscription(String subscriptionId);

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE read = 0 and subscriptionId = :subscriptionId ORDER by published")
    FeedItemEntity[] getUnreadItemsForSubscriptionOlderToNewer(String subscriptionId);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE favorite = 1 and subscriptionId = :subscriptionId ORDER by published DESC")
    FeedListItemModel[] getFavoriteFeedListModelsForSubscription(String subscriptionId);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE favorite = 1 and subscriptionId = :subscriptionId ORDER by published")
    FeedListItemModel[] getFavoriteFeedListModelsForSubscriptionOlderToNewer(String subscriptionId);

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 and subscriptionId = :subscriptionId ORDER by published DESC")
    FeedItemEntity[] getFavoriteFeedItemsForSubscription(String subscriptionId);

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = 1 and subscriptionId = :subscriptionId ORDER by published")
    FeedItemEntity[] getFavoriteFeedItemsForSubscriptionOlderToNewer(String subscriptionId);


    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE feed_items.read = 0 ORDER by feed_items.published DESC")
    FeedListItemModel[] getUnreadFeedListModelsForTag(String name);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE feed_items.read = 0 ORDER by feed_items.published")
    FeedListItemModel[] getUnreadFeedListModelsForTagOlderToNewer(String name);



    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " " +
            "INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " WHERE feed_items.read = 0 ORDER by feed_items.published DESC")
    FeedItemEntity[] getUnreadFeedItemsForTag(String name);


    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name " +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " ORDER by feed_items.published DESC")
    FeedListItemModel[] getAllFeedListModelsForTag(String name);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name " +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " ORDER by feed_items.published")
    FeedListItemModel[] getAllFeedListModelsForTagOlderToNewer(String name);

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " " +
            "INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name " +
            "ORDER by feed_items.published DESC")
    FeedItemEntity[] getAllFeedItemsForTag(String name);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE feed_items.favorite = 1 ORDER by feed_items.published DESC")
    FeedListItemModel[] getFavFeedListModelsForTag(String name);

    @Query("SELECT " +
            "feed_items.id AS id, " +
            "feed_items.link AS link, " +
            "feed_items.title AS title, " +
            "feed_items.excerpt AS excerpt, " +
            "feed_items.published AS published, " +
            "subscriptions.title AS subscriptionName, " +
            "feed_items.read AS read, " +
            "feed_items.favorite AS favorite, " +
            "feed_items.leadImgPath AS leadImgPath, " +
            "subscriptions.iconUrl AS subscriptionIcon" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE +
            " INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " INNER JOIN subscriptions ON subscriptions.id = feed_items.subscriptionId" +
            " WHERE feed_items.favorite = 1 ORDER by feed_items.published")
    FeedListItemModel[] getFavFeedListModelsForTagOlderToNewer(String name);

    @Query("SELECT *" +
            " FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " " +
            "INNER JOIN tags ON tags.subscriptionId = feed_items.subscriptionId and tags.name = :name" +
            " WHERE feed_items.favorite = 1 ORDER by feed_items.published DESC")
    FeedItemEntity[] getFavFeedItemsForTag(String name);

    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE subscriptionId = :subscriptionId AND syncedAt > :since AND read = 0 ORDER by syncedAt DESC LIMIT 100")
    FeedItemEntity[] getFeedItemsForSubscriptionSyncAt(String subscriptionId, long since);

    @Query("SELECT id FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE modifiedAt > syncedAt AND read = :read")
    int[] getModifiedFeedItemsSinceLastSync(boolean read);


    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE modifiedAt > syncedAt AND read = :read")
    FeedItemEntity[] getModifiedFeedEntitiesSinceLastSync(boolean read);

    @Query("SELECT * FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE favorite = :fav AND  modifiedAt > syncedAt")
    FeedItemEntity[] getFavedFeedItemEntitySinceLastSync(boolean fav);

    @Query("SELECT id FROM " + ReadablyDatabase.FEED_ITEMS_TABLE + " WHERE modifiedAt > syncedAt AND favorite = :fav")
    int[] getFavedFeedItemsSinceLastSync(boolean fav);

    @Query("UPDATE " + ReadablyDatabase.FEED_ITEMS_TABLE + " SET read = 1, modifiedAt = :modifiedTime WHERE subscriptionId = :subscriptionId")
    void markSubscriptionRead(String subscriptionId, long modifiedTime);

    @Query("UPDATE " + ReadablyDatabase.FEED_ITEMS_TABLE + " SET read = 1, modifiedAt = :modifiedTime")
    void markEverythingAsRead(long modifiedTime);

    @Query("UPDATE " + ReadablyDatabase.FEED_ITEMS_TABLE + " SET read = :read, modifiedAt = :modifiedTime WHERE id = :feedItemId")
    void toggleFeedItemReadStatus(String feedItemId, boolean read, long modifiedTime);


    @Query("UPDATE " + ReadablyDatabase.FEED_ITEMS_TABLE + " SET favorite = :favorite, modifiedAt = :modifiedTime WHERE id = :feedItemId")
    void toggleFeedItemFavStatus(String feedItemId, boolean favorite, long modifiedTime);


    /**
     * {@link SubscriptionEntity} related operations
     */

    @Insert
    void newSubscription(SubscriptionEntity subscriptionEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSubscriptions(List<SubscriptionEntity> subscriptionEntities);

    @Update
    void updateSubscription(SubscriptionEntity subscriptionEntity);

    @Delete
    void deleteSubscription(SubscriptionEntity subscriptionEntity);

    @Query("SELECT * FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE)
    SubscriptionEntity[] getAllSubscriptions();

    @Query("SELECT * FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE)
    LiveData<SubscriptionEntity[]> getAllSubscriptionsLiveData();

    @Query("SELECT * FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE + " WHERE id = :subscriptionId")
    SubscriptionEntity getSubscription(String subscriptionId);

    @Query("SELECT * FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE + " WHERE id IN (:subscriptionIds)")
    SubscriptionEntity[] getSubscriptions(List<String> subscriptionIds);

    @Query("SELECT id FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE)
    List<String> getAllSubscriptionIds();


    @Query("SELECT " +
            "subscriptions.id, " +
            "subscriptions.title AS title, " +
            "subscriptions.iconUrl AS iconUrl " +
            "FROM tags " +
            "INNER JOIN subscriptions ON subscriptions.id = tags.subscriptionId " +
            "WHERE tags.name = :name ORDER by title")
    List<NavigationSubscriptionItemModel> getNavigationSubscriptionsForTag(String name);


    @Query("SELECT " +
            "subscriptions.id AS id, " +
            "subscriptions.title AS title, " +
            "subscriptions.iconUrl AS iconUrl " +
            "FROM subscriptions " +
            "WHERE NOT EXISTS (SELECT t.subscriptionId FROM tags t WHERE subscriptions.id == t.subscriptionId)")
    List<NavigationSubscriptionItemModel> getUntaggedNavigationSubscriptions();


    @Query("SELECT DISTINCT " +
            "tags.name AS name " +
            "FROM tags ORDER by name")
    List<NavigationTagItemModel> getNavigationTags();


    @Query("SELECT * "
            + "FROM subscriptions, tags "
            + "WHERE tags.name = :name and tags.subscriptionId = subscriptions.id")
    List<SubscriptionEntity> getSubscriptionsWithTag(String name);


    @Query("SELECT count(feed_items.read) FROM subscriptions " +
            "INNER JOIN tags ON tags.subscriptionId = subscriptions.id and tags.name = :name " +
            "INNER JOIN feed_items ON subscriptions.id = feed_items.subscriptionId")
    int[] getUnreadCountsForTag(String name);


    @Query("SELECT count(feed_items.read) FROM feed_items " +
            "WHERE feed_items.subscriptionId = :subscriptionId and feed_items.read = 0")
    int getUnreadCountForSubscription(String subscriptionId);


    @Query("SELECT count(feed_items.read) FROM feed_items " +
            "WHERE feed_items.subscriptionId = :subscriptionId and feed_items.favorite = 1")
    int getFavCountForSubscription(String subscriptionId);


    @Query("SELECT count(feed_items.read) FROM feed_items WHERE feed_items.read = 0")
    int getAllSubscriptionsUnreadCount();

    @Query("SELECT count(feed_items.read) FROM feed_items WHERE feed_items.favorite = 1")
    int getAllSubscriptionsFavCount();


    /**
     * {@link TagEntity} operations
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void newTag(TagEntity tagEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addTags(List<TagEntity> tagEntities);

    @Update
    void updateTag(TagEntity tagEntity);

    @Delete
    void deleteTags(List<TagEntity> tagEntities);

    @Delete
    void deleteTag(TagEntity tagEntity);

    @Query("DELETE FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE subscriptionId = :subscriptionId AND name = :name")
    void deleteTagByServerId(String subscriptionId, String name);

    @Query("SELECT name FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE subscriptionId = :subscriptionId")
    List<String> getSubscriptionTagNames(String subscriptionId);

    @Query("SELECT * FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE subscriptionId = :subscriptionId")
    List<TagEntity> getSubscriptionTags(String subscriptionId);

    @Query("SELECT subscriptionId FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE name = :name")
    List<String> getSubscriptionIdsForTag(String name);


    @Query("SELECT * FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE name = :name")
    List<TagEntity> getTagByName(String name);

    @Query("SELECT * FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE serverId = :serverId")
    TagEntity getTagByServerId(String serverId);

    @Query("SELECT DISTINCT * FROM " + ReadablyDatabase.TAGS_TABLE)
    LiveData<TagEntity[]> getDistinctTagsLiveData();

    @Query("SELECT * FROM " + ReadablyDatabase.TAGS_TABLE)
    List<TagEntity> getAllTags();

    @Query("SELECT  DISTINCT id FROM " + ReadablyDatabase.SUBSCRIPTIONS_TABLE)
    List<String> getTaggedSubscriptionIds();

    @Query("SELECT DISTINCT name FROM " + ReadablyDatabase.TAGS_TABLE + " ORDER by name")
    List<String> getTagNames();


    @Query("SELECT * FROM subscriptions s1 WHERE NOT EXISTS (SELECT t.subscriptionId FROM tags t WHERE s1.id == t.subscriptionId)")
    List<SubscriptionEntity> getUntaggedSubscriptions();

    @Query("SELECT DISTINCT name FROM " + ReadablyDatabase.TAGS_TABLE + " ORDER by name")
    LiveData<List<String>> getTagNamesLiveData();

    @Query("SELECT * FROM " + ReadablyDatabase.TAGS_TABLE + " WHERE subscriptionId = :subscriptionId and name = :name")
    TagEntity getTag(String subscriptionId, String name);

    /*Check if old images-caches exist and return the results */
    @Query("SELECT * FROM feed_items WHERE id IN (SELECT id FROM feed_items WHERE subscriptionId = :subscriptionId AND favorite = 0 AND read = :read ORDER BY published DESC LIMIT -1 OFFSET :offset)")
    List<FeedItemEntity> getExcessItems(String subscriptionId, int offset, boolean read);


//    /*final House keeping */
//    @Query("DELETE FROM feed_items WHERE id not in (SELECT * FROM (SELECT id FROM feed_items where subscriptionId = :subscriptionId And favorite != 1 ORDER BY published desc limit :limit))")
//    int finalHouseKeeping(int subscriptionId ,int limit);


    // Delete all feed items
    @Query("DELETE FROM feed_items")
    void deleteAllFeedItems();

    @Query("DELETE FROM subscriptions")
    void deleteAllSubscriptions();

    @Query("DELETE FROM tags")
    void deleteAllTags();

}

