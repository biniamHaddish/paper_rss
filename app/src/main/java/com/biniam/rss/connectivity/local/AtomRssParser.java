package com.biniam.rss.connectivity.local;

import android.content.Context;
import android.util.Log;

import com.biniam.rss.models.local.atom.Atom;
import com.biniam.rss.models.local.atom.AtomFeed;
import com.biniam.rss.models.local.rss.Rss;
import com.biniam.rss.models.local.rss.RssFeed;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.utils.Utils;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by biniam_Haddish on 2/24/17.
 * <p>
 * Parses channel/atom xml into usable forms for the rest of the app
 */

public class AtomRssParser {

    public static final String TAG = AtomRssParser.class.getSimpleName();

    private static AtomRssParser atomRssParser;
    private Serializer serializer;
    private FeedParser feedParser;

    private AtomRssParser(Context context) {
        feedParser = FeedParser.getInstance(context);
        serializer = new Persister();
    }

    public static AtomRssParser getInstance(Context context) {
        if (atomRssParser == null) atomRssParser = new AtomRssParser(context);
        return atomRssParser;
    }

    private FeedItemEntity convert(AtomFeed atomFeed, SubscriptionEntity subscriptionEntity) {

        if (isValid(atomFeed)) {

            Date atomPublishedDate = null;

            // Parse the atom feed published property to a timestamp
            if (atomFeed.published != null) {
                try {
                    atomPublishedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(atomFeed.published);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


            FeedItemEntity feedItemEntity = new FeedItemEntity(
                    atomFeed.title,
                    subscriptionEntity.id,
                    Utils.getSHA1Digest(atomFeed.link.iterator().next().href + atomFeed.id),
                    atomPublishedDate != null ? atomPublishedDate.getTime() : System.currentTimeMillis(),
                    atomFeed.contentEncoded != null && !atomFeed.contentEncoded.isEmpty() ? atomFeed.contentEncoded : atomFeed.content,
                    null,
                    atomFeed.link.iterator().next().href,
                    subscriptionEntity.title
            );

            feedItemEntity.syncedAt = System.currentTimeMillis();
            feedItemEntity.author = atomFeed.author.name;
            return feedParser.parseFeedItem(feedItemEntity);
        }

        return null;
    }

    private FeedItemEntity convert(RssFeed rssFeed, SubscriptionEntity subscriptionEntity) {
        if (isValid(rssFeed)) {
            Date rssPubDate = null;

            Log.d(TAG, String.format("convert: channel feed content encoded is %s", rssFeed.contentEncoded));

            try {
                rssPubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(rssFeed.pubDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            FeedItemEntity feedItemEntity = new FeedItemEntity(
                    rssFeed.title,
                    subscriptionEntity.id,
                    Utils.getSHA1Digest(rssFeed.link + (rssFeed.guid == null ? rssFeed.title : rssFeed.guid)),
                    rssPubDate != null ? rssPubDate.getTime() : System.currentTimeMillis(),
                    rssFeed.contentEncoded != null && !rssFeed.contentEncoded.isEmpty() ? rssFeed.contentEncoded : rssFeed.description,
                    null,
                    rssFeed.link,
                    subscriptionEntity.title
            );

            feedItemEntity.syncedAt = System.currentTimeMillis();
            feedItemEntity.author = rssFeed.author;
            return feedParser.parseFeedItem(feedItemEntity);
        }

        return null;
    }


    // Checks whether this atom feed contains the minimum required properties
    private boolean isValid(AtomFeed atomFeed) {
        return (atomFeed.id != null && !atomFeed.id.isEmpty()) &&
                (atomFeed.title != null && !atomFeed.title.isEmpty()) &&
                (atomFeed.content != null && !atomFeed.content.isEmpty()) &&
                (atomFeed.link != null && !atomFeed.link.isEmpty());

    }


    // Checks whether this channel feed contains the minimum required properties
    private boolean isValid(RssFeed rssFeed) {
        return (
                (rssFeed.title != null && !rssFeed.title.isEmpty()) &&
                ((rssFeed.description != null && !rssFeed.description.isEmpty()) ||
                        (rssFeed.contentEncoded != null && !rssFeed.contentEncoded.isEmpty())) &&
                        (rssFeed.link != null && !rssFeed.link.isEmpty()));
    }


    public List<FeedItemEntity> getFeedItems(String xmlString, SubscriptionEntity subscriptionEntity) throws Exception {

        ArrayList<FeedItemEntity> feeds = new ArrayList<>();

//        if (serializer.validate(RssVar.class, xmlString, false)) {
//            RssVar rssVar = serializer.read(RssVar.class, xmlString, false);
//            if (rssVar != null && rssVar.channel.items != null) {
//                Log.d(TAG, String.format("getFeedItems: title is %s", rssVar.channel.title));
//                for (RssFeed feed : rssVar.channel.items) {
//                    if (feed != null) {
//                        FeedItemEntity convertedFeed = convert(feed, subscriptionEntity);
//                        if (convertedFeed != null) feeds.add(convertedFeed);
//                    }
//                }
//                return feeds;
//            }
//        }


        if (serializer.validate(Atom.class, xmlString, true)) {

            Atom atom = serializer.read(Atom.class, xmlString, false);
            if (atom != null && atom.feeds != null) {
                Log.d(TAG, "getFeedItems: valid atom");
                for (AtomFeed feed : atom.feeds) {
                    Log.d(TAG, String.format("getFeedItems: content:encoded is %s", feed.contentEncoded));
                    if (feed != null) {
                        FeedItemEntity convertedFeed = convert(feed, subscriptionEntity);
                        if (convertedFeed != null) feeds.add(convertedFeed);
                    }
                }

                return feeds;
            }

        }

        if (serializer.validate(Rss.class, xmlString, true)) {

            Rss rss = serializer.read(Rss.class, xmlString, true);
            if (rss != null && rss.channel != null) {
                Log.d(TAG, String.format("getFeedItems: valid rss, it has %d items", rss.channel.size()));
                for (RssFeed feed : rss.channel) {
                    if (feed != null) {
                        Log.d(TAG, String.format("getFeedItems: feed item title is %s", feed.title));
                        FeedItemEntity convertedFeed = convert(feed, subscriptionEntity);
                        if (convertedFeed != null) feeds.add(convertedFeed);
                    }
                }


                return feeds;
            }
        }


//            Log.d(TAG, "getFeedItems: RssVar is valid");
//            RssVar rss = serializer.read(RssVar.class, xmlString, false);
//            if (rss != null && rss.channel.items != null){
//                for (RssFeed feed: rss.channel.items){
//                    if (feed != null){
//                        FeedItemEntity convertedFeed = convert(feed, subscriptionEntity);
//                        if (convertedFeed != null) feeds.add(convertedFeed);
//                    }
//                }
//                return feeds;
//            }
//        }

        return null;
    }


}
