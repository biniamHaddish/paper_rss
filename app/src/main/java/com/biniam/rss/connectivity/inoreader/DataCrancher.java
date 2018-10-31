package com.biniam.rss.connectivity.inoreader;

import android.content.Context;
import android.util.Log;

import com.biniam.rss.models.inoreader.InoStreamContentList;
import com.biniam.rss.models.inoreader.InoSubscriptionList;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.persistence.preferences.PaperPrefs;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.utils.PaperApp;

import java.util.ArrayList;
import java.util.List;

public class DataCrancher {

    private String TAG= DataCrancher.class.getSimpleName();
    private static DataCrancher dataCrancher;
    private PaperDatabase rssDatabase;
    private PaperPrefs paperPrefs;
    private Context mContext;


    /*Constructor for the Class */
    public DataCrancher(Context context){
        mContext = context;
        rssDatabase = PaperApp.getInstance().getDatabase();
        paperPrefs = PaperPrefs.getInstance(context);
    }
    /*getting the instance of the Class*/
    public static DataCrancher getInstance(Context context){
        if (dataCrancher!=null){
            dataCrancher= new DataCrancher(context);
        }
        return dataCrancher;
    }

    /**
     * Will give us the proper InoReader DateTime Conversion.
     *
     * @param time
     * @return
     */
    private long setInoReaderProperDateTime(long time) {
        return (Long.valueOf(time).longValue()) * 1000;
    }

    /**
     * will get the Last part of the id
     *
     * @param id
     * @return
     */
    private String getIdLastPath(String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    /**
     * @param subsId
     * @return
     */
    private String replaceSlashWithDash(String subsId) {
        return subsId.replace("/", "_").trim();
    }

    /**
     * @param subsId
     * @return
     */
    private String replaceDashWithSlash(String subsId) {
        return subsId.replace("_", "/").trim();
    }

    /**
     * @param lastId
     * @return
     */
    private String appendFeedIdProperId(String lastId) {
        return "tag:google.com,2005:reader/item/" + lastId.trim();
    }


    /**
     * Adding the starred items to the database
     *
     * @param itemsBeans
     * @return
     */
    public List<FeedItemEntity> addStarredInoreaderEntries(InoStreamContentList itemsBeans) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {

            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));

            if (existing != null) {
                existing.favorite = true;
                rssDatabase.dao().updateFeedItem(existing);

            } else {

                for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,
                            alternateBean.getHref(),
                            inoFeedItems.getOrigin().getTitle()
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity.modifiedAt = 0;
                    feedItemEntity.favorite = true;
                    feedItemEntity.read = false;
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                }
            }
        }
        return feedItemEntities;
    }
    /**
     * @param itemsBeans
     * @return
     */
    public List<FeedItemEntity> addAllInoReaderEntries(InoStreamContentList itemsBeans) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {

            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            SubscriptionEntity subscriptionEntity = rssDatabase.dao()
                    .getSubscription(replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()));

            for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                if (existing == null && subscriptionEntity != null) {
                    Log.d(TAG, "StreamId: \t" + inoFeedItems.getOrigin().getStreamId());
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,//this the excerpt cuz inoreader does not support excerpt at the moment.
                            alternateBean.getHref(),
                            subscriptionEntity.title
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                } else if (existing != null) {
                    rssDatabase.dao().updateFeedItem(existing);
                }
            }
        }
        return feedItemEntities;
    }


    /**
     * @param itemsBeans
     * @param subscriptionId
     * @return
     */
    public List<FeedItemEntity> addInoReaderEntries(List<InoStreamContentList.ItemsBean> itemsBeans, String subscriptionId) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans) {
            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            SubscriptionEntity subscriptionEntity = rssDatabase.dao().getSubscription(subscriptionId);

            for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                if (existing == null && subscriptionEntity != null) {
                    Log.d(TAG, "addInoReaderEntries: " + subscriptionId);
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(subscriptionId),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,//this the excerpt cuz inoreader does not support excerpt at the moment.
                            alternateBean.getHref(),
                            subscriptionEntity.title
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                } else if (existing != null) {
                    rssDatabase.dao().updateFeedItem(existing);
                }
            }
        }
        return feedItemEntities;
    }

    /**
     * @param subscriptionsBeans
     * @return
     */
    public List<TagEntity> addInoReaderTags(List<InoSubscriptionList.SubscriptionsBean> subscriptionsBeans) {

        List<TagEntity> tagEntities = new ArrayList<>();

        for (InoSubscriptionList.SubscriptionsBean tags : subscriptionsBeans) {
            for (InoSubscriptionList.SubscriptionsBean.CategoriesBean categoriesBean : tags.getCategories()) {
                TagEntity tagEntity = new TagEntity();
                tagEntity.subscriptionId = tags.getSubId();
                tagEntity.name = categoriesBean.getLabel();
                tagEntity.serverId = categoriesBean.getId();
                tagEntities.add(tagEntity);
            }
        }
        return tagEntities;
    }

    /**
     * Adding the InoReader List of Subscriptions to Room Table by matching the column and rows to fit the table at Room
     *
     * @param inoSubscriptionLists
     * @return
     */
    public List<SubscriptionEntity> AddInoReaderSubscriptionToRoom(List<InoSubscriptionList.SubscriptionsBean> inoSubscriptionLists) {
        // init the entities
        List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        //InoSubscriptionList.SubscriptionsBean subscription:
        for (InoSubscriptionList.SubscriptionsBean inoSubscriptionListItems : inoSubscriptionLists) {
            Log.d(TAG, "AddInoReaderSubscriptionToRoom: " + inoSubscriptionListItems.getSubId());
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
            subscriptionEntity.id = inoSubscriptionListItems.getSubId();
            subscriptionEntity.title = inoSubscriptionListItems.getTitle();
            subscriptionEntity.siteLink = inoSubscriptionListItems.getHtmlUrl();
            subscriptionEntity.rssLink = inoSubscriptionListItems.getUrl();
            subscriptionEntity.createdTimestamp = inoSubscriptionListItems.getFirstitemmsec();
            subscriptionEntities.add(subscriptionEntity);// adding the final result to the room subscription table
        }
        return subscriptionEntities;
    }



}
