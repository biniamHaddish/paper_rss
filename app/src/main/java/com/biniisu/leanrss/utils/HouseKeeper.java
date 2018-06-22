package com.biniisu.leanrss.utils;

import android.content.Context;
import android.util.Log;

import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.persistence.preferences.ReadablyPrefs;

import java.io.File;
import java.util.List;

/**
 * Created by biniam_Haddish on 1/30/18.
 *<p> Biniam : made few changes  with the code deletes the folders and files inside the caches Dir.</p>
 */

public class HouseKeeper {

    public static final String TAG = HouseKeeper.class.getSimpleName();
    private static HouseKeeper houseKeeper;
    private static ReadablyDatabase rssDatabase;
    private Context mContext;
    private ReadablyPrefs readablyPrefs;

    /**
     * @param context
     */
    private HouseKeeper(Context context) {
        mContext = context;
        rssDatabase = ReadablyApp.getInstance().getDatabase();
        readablyPrefs = ReadablyPrefs.getInstance(context);
    }

    /**
     *
     * @param context
     * @return
     */
    public static HouseKeeper getInstance(Context context) {
        if (houseKeeper == null) {
            houseKeeper = new HouseKeeper(context);
        }
        return houseKeeper;
    }

    /**
     * Deletes excess feed items along with their cached images
     */
    public void deleteOldCaches() {

        SubscriptionEntity[] subscriptionEntities = rssDatabase.dao().getAllSubscriptions();
        for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {

            try {

                List<FeedItemEntity> excessReadFeedItems = rssDatabase.dao().getExcessItems(String.valueOf(subscriptionEntity.id), readablyPrefs.readItemsToKeep, true);
                List<FeedItemEntity> excessUnreadFeedItems = rssDatabase.dao().getExcessItems(String.valueOf(subscriptionEntity.id), readablyPrefs.unreadItemsToKeep, false);

                    cleanUpExcessFeedItems(subscriptionEntity, excessReadFeedItems);
                    cleanUpExcessFeedItems(subscriptionEntity, excessUnreadFeedItems);

            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException: " + e.getMessage());
            }
        }
    }

    /**
     * Will delete all the files and dir of the images.
     * @param subscriptionEntity
     * @param excessFeedItems
     */
    private void cleanUpExcessFeedItems(SubscriptionEntity subscriptionEntity, List<FeedItemEntity> excessFeedItems) {

        File subscriptionDir = new File(mContext.getCacheDir(), String.valueOf(subscriptionEntity.id));
        for (FeedItemEntity excessFeedItemEntity : excessFeedItems) {
            File excessFeedItemDir = new File(subscriptionDir, String.valueOf(excessFeedItemEntity.id));
            Log.e(TAG, "PathToSubscriptionDir \t: " + excessFeedItemDir);
            if (excessFeedItemDir.exists() && excessFeedItemDir.isDirectory()) {
                Log.d(TAG, "cleanUpExcessFeedItems: " + excessFeedItemDir.exists());
                deleteRecursive(excessFeedItemDir);// will take care of all the data deletion and the Dir.
            }
        }
        rssDatabase.dao().deleteFeedItems(excessFeedItems);
    }


    /**
     * Will delete the files and the Folders(dir) recursively and will finally delete the dir (Folder)
     * @param fileOrDirectory
     */
    private void deleteRecursive(File fileOrDirectory) {
        Log.d(TAG, "deleteRecursive: \t" + fileOrDirectory);
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        //finally delete the directory (folder)
        fileOrDirectory.delete();
        Log.d(TAG, "total_Cache_Space_While_deleting: " + fileOrDirectory.getTotalSpace() / 1024 * 1024);
    }
}
