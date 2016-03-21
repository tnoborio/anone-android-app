package com.halake.anone;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getSimpleName();
    private NotificationManager nm;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d(TAG, "messageType: " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(TAG, "messageType: " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(TAG, "messageType: " + messageType + ",body:" + extras.toString());
            }
        }

        Log.v(TAG, extras.toString());
        String message = extras.getString("message");

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setType("notification");
        i.putExtra("message", message);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification =
                new Notification.Builder(getApplicationContext())
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.small_icon)
                        .setTicker("通知がきました!")
                        .setContentTitle("anone")
                        .setContentText(message).build();

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, notification);

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nm.cancel(R.string.app_name);
    }
}
