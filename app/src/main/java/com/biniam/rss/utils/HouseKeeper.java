package com.biniam.rss.utils;

import android.content.Context;
import android.util.Log;

import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.preferences.PaperPrefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by biniam_Haddish on 1/30/18.
 * <p> Biniam : made few changes  with the code deletes the folders and files inside the caches Dir.</p>
 */

public class HouseKeeper {

    public static final String TAG = HouseKeeper.class.getSimpleName();
    private static HouseKeeper houseKeeper;
    private static PaperDatabase rssDatabase;
    private Context mContext;
    private PaperPrefs paperPrefs;

    /**
     * @param context
     */
    private HouseKeeper(Context context) {
        mContext = context;
        rssDatabase = PaperApp.getInstance().getDatabase();
        paperPrefs = PaperPrefs.getInstance(context);
    }

    /**
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
     * Will delete all the files and dir of the images.
     *
     * @param subscriptionEntity
     * @param excessFeedItems
     */
    public void imagesCacheDirStr(SubscriptionEntity subscriptionEntity, List<String> excessFeedItems) {

        File subscriptionDir = new File(mContext.getCacheDir(), String.valueOf(subscriptionEntity.id));
        for (String excessFeedItemEntity : excessFeedItems) {
            File excessFeedItemDir = new File(subscriptionDir, excessFeedItemEntity);
            Log.d(TAG, "PathToSubscriptionDir  \t: " + excessFeedItemDir);
            if (excessFeedItemDir.exists() && excessFeedItemDir.isDirectory()) {
                Log.d(TAG, "pathToImageFile: " + excessFeedItemDir);
                deleteRecursive(excessFeedItemDir);// will take care of all the data deletion and the Dir.
            }
        }
    }

    /**
     * Will delete the Data from the database
     *
     * @param timeBorder
     */
    public void deleteOldEntries(long timeBorder) {
        if (timeBorder > 0) {
            int countDeletedItems = rssDatabase.dao().deleteOlderThan(timeBorder);
            if (countDeletedItems > 0) {
                Log.d(TAG, "ItemsDeleted-Deleted-Count\t" + countDeletedItems);
            }
        }
    }

    /**
     * collect all the Ids of the Image cache to Be Deleted
     *
     * @param timeBorder
     */
    public List<String> getImagesCacheIds(long timeBorder) {
        List<String> feedItemEntitiesId = new ArrayList<>();
        if (timeBorder > 0) {
            feedItemEntitiesId = rssDatabase.dao().getImagesCache(timeBorder);
            if (feedItemEntitiesId.size() > 0) {
                for (String feedIds : feedItemEntitiesId) {
                    Log.d(TAG, "getImagesCacheIds: " + feedIds);
                }
            }
        }
        return feedItemEntitiesId;
    }

    /**
     * Will delete the files and the Folders(dir) recursively and will finally delete the dir (Folder)
     *
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
