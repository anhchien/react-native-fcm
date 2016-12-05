package com.evollu.react.fcm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    public Bundle toBundle(RemoteMessage message) {
        Bundle bundle = new Bundle();
        Bundle params = new Bundle();
        params.putString("priority", "max");
        params.putString("sound", "default");
        params.putLong("vibrate", 300);
        params.putBoolean("lights", true);
        params.putBoolean("show_in_foreground", true);
        params.putBoolean("auto_cancel", true);
        params.putString("id", "system_message");

        List<String> fcm = Arrays.asList(new String[] {"title", "body", "color", "icon", "tag", "action"});

        if(message.getData() != null){
            Map<String, String> data = message.getData();
            Set<String> keysIterator = data.keySet();
            for(String key: keysIterator){
                bundle.putString(key, data.get(key));
                if(fcm.contains(key)) {
                    params.putString(key, data.get(key));
                }
            }
        }

        if (message.getNotification() != null) {
            RemoteMessage.Notification notification = message.getNotification();
            params.putString("title", notification.getTitle());
            params.putString("body", notification.getBody());
            params.putString("color", notification.getColor());
            params.putString("icon", notification.getIcon());
            params.putString("tag", notification.getTag());
            params.putString("action", notification.getClickAction());
        }
        params.putBundle("data", bundle);
        return params;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");
        Intent i = new Intent("com.evollu.react.fcm.ReceiveNotification");
        i.putExtra("data", remoteMessage);
        sendOrderedBroadcast(i, null);
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            boolean fcm_enable_custom_notification = bundle.getBoolean("com.aotasoft.fcm.customNotification", false);
            if(fcm_enable_custom_notification) {
                FIRLocalMessagingHelper localMessagingHelper = new FIRLocalMessagingHelper(getApplication());
                remoteMessage.getData();
                Bundle messageBundle = toBundle(remoteMessage);
                localMessagingHelper.sendNotification(messageBundle);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
