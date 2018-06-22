package com.biniam.rss.utils;


import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.biniam.rss.R;
import com.biniam.rss.connectivity.local.LocalSyncJobService;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.ReadablyPrefs;
import com.biniam.rss.utils.AutoSyncManagers.FeedBinSyncJobService;
import com.biniam.rss.utils.AutoSyncManagers.FeedWranglerSyncJobService;
import com.biniam.rss.utils.AutoSyncManagers.InoReaderSyncService;

/**
 * Created by biniam_Haddish on 26/1/18.
 * <p>
 * This class handles provides properties related to the currently logged-in account
 */

public class AccountBroker {

    public static final String ACTION_SYNC_STARTED = "ACTION_SYNC_STARTED";
    public static final String ACTION_SYNC_STAGE_SUBSCRIPTIONS = "ACTION_SYNC_STAGE_SUBSCRIPTIONS";
    public static final String ACTION_SYNC_STAGE_ITEMS = "ACTION_SYNC_STAGE_ITEMS";
    public static final String ACTION_SYNC_STAGE_IMAGES = "ACTION_SYNC_STAGE_IMAGES";
    public static final String ACTION_SYNC_FINISHED = "ACTION_SYNC_FINISHED";
    public static final String ACTION_SEND_NOTIFICATION = "ACTION_SEND_NOTIFICATION";

    public static AccountBroker accountBroker;
    private Context context;
    private ReadablyPrefs readablySettings;
    private InternalStatePrefs internalStatePrefs;

    private AccountBroker(Context context) {
        this.context = context;
        readablySettings = ReadablyPrefs.getInstance(context);
        internalStatePrefs = InternalStatePrefs.getInstance(context);
    }

    public static AccountBroker getInstance(Context context) {
        if (accountBroker == null) {
            accountBroker = new AccountBroker(context);
        }

        return accountBroker;
    }

    public void scheduleAccountJob() {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (getAccountJobId() < 0) {
            return;
        }

        JobInfo jobInfo = new JobInfo.Builder(getAccountJobId(), getComponentName())
                .setRequiredNetworkType(readablySettings.automaticSyncWiFiOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(readablySettings.syncInterval * 1000)
                .setPersisted(true)
                .build();

        jobScheduler.schedule(jobInfo);
    }

    public void cancelAccountJob() {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }


    private int getAccountJobId() {
        switch (internalStatePrefs.currentAccount) {
            case InternalStatePrefs.LOCAL_ACCOUNT:
                return LocalSyncJobService.JOB_ID;
            case InternalStatePrefs.FEED_BIN_ACCOUNT:
                return FeedBinSyncJobService.JOB_ID;
            case InternalStatePrefs.FEED_WRANGLER_ACCOUNT:
                return FeedWranglerSyncJobService.JOB_ID;
            case InternalStatePrefs.INOREADER_ACCOUNT:
                return InoReaderSyncService.JOB_ID;
        }

        return -1;
    }

    public int getAccountLogoRes() {
        switch (internalStatePrefs.currentAccount) {
            case InternalStatePrefs.LOCAL_ACCOUNT:
                return R.drawable.ic_rss_feed_24px;
            case InternalStatePrefs.FEED_BIN_ACCOUNT:
                return R.drawable.ic_feedbin_logo;
            case InternalStatePrefs.INOREADER_ACCOUNT:
                return R.drawable.inoreader_logo;
            case InternalStatePrefs.FEED_WRANGLER_ACCOUNT:
                return R.drawable.ic_feed_wrangler_logo;
        }

        return -1;
    }


    public int getAccountNameRes() {
        switch (internalStatePrefs.currentAccount) {
            case InternalStatePrefs.LOCAL_ACCOUNT:
                return R.string.local_account_name;
            case InternalStatePrefs.FEED_BIN_ACCOUNT:
                return R.string.feedbin_account_name;
            case InternalStatePrefs.FEED_WRANGLER_ACCOUNT:
                return R.string.FeedWrangler_account_name;
            case InternalStatePrefs.INOREADER_ACCOUNT:
                return R.string.InoReader_account_name;
        }

        return -1;
    }

    public ComponentName getComponentName() {
        switch (internalStatePrefs.currentAccount) {
            case InternalStatePrefs.LOCAL_ACCOUNT:
                return new ComponentName(context, LocalSyncJobService.class);
            case InternalStatePrefs.FEED_BIN_ACCOUNT:
                return new ComponentName(context, FeedBinSyncJobService.class);
            case InternalStatePrefs.INOREADER_ACCOUNT:
                return new ComponentName(context, InoReaderSyncService.class);
            case InternalStatePrefs.FEED_WRANGLER_ACCOUNT:
                return new ComponentName(context, FeedWranglerSyncJobService.class);
        }
        return null;
    }


    public Class getAccountServiceClass() {
        switch (internalStatePrefs.currentAccount) {
            case InternalStatePrefs.LOCAL_ACCOUNT:
                return LocalSyncJobService.class;
            case InternalStatePrefs.FEED_BIN_ACCOUNT:
                return FeedBinSyncJobService.class;
            case InternalStatePrefs.INOREADER_ACCOUNT:
                return InoReaderSyncService.class;
            case InternalStatePrefs.FEED_WRANGLER_ACCOUNT:
                return FeedWranglerSyncJobService.class;
        }
        return null;
    }

    public boolean isCurrentAccountFeedBin() {
        return internalStatePrefs.currentAccount == InternalStatePrefs.FEED_BIN_ACCOUNT;
    }

    public boolean isCurrentAccountLocal() {
        return InternalStatePrefs.LOCAL_ACCOUNT == internalStatePrefs.currentAccount;
    }

    public boolean isCurrentAccountInoreader() {
        return internalStatePrefs.currentAccount == InternalStatePrefs.INOREADER_ACCOUNT;
    }

    public boolean isCurrentAccountFeedWrangler() {
        return InternalStatePrefs.FEED_WRANGLER_ACCOUNT == internalStatePrefs.currentAccount;
    }

    public boolean isAccountSyncServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (getAccountServiceClass().getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}