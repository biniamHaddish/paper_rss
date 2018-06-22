package com.biniam.rss.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import com.biniam.rss.R;
import com.biniam.rss.ui.base.HomeActivity;

/**
 * Created by biniam on 3/16/18.
 */
@TargetApi(Build.VERSION_CODES.O)
public class FeedSyncNotificationManager extends ContextWrapper {


    public String notification_id = "READABLY_NOTIFICATION_ID";
    Context context;
    private NotificationManager notificationManager;

    /**
     * constructor
     *
     * @param ctx
     */
    public FeedSyncNotificationManager(Context ctx) {
        super(ctx);
        createChannel();
    }

    /**
     *
     *
     */
    public void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.readably_notification_name);
        // The user-visible description of the channel.
        String description = getString(R.string.readably_notification_body);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(notification_id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setSound(null, null);
        mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    /**
     * Build notification for secondary channel.
     *
     * @param title Title for notification.
     * @param body  Message for notification.
     * @return A Notification.Builder configured with the selected channel and details
     */
    public Notification.Builder getNotification(String title, String body) {
        Intent intent = new Intent(this, HomeActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);
        return new Notification.Builder(getApplicationContext(), notification_id)
                //.addAction(new Notification.Action(0, "Unread Articles", pendingIntent))
                .setContentTitle(title)
                .setContentText(body)
                .setColor(getColor(R.color.merino))
                .setSmallIcon(getSmallIcon());
    }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return R.drawable.readably_icon_minimal;
    }

    /**
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

}
