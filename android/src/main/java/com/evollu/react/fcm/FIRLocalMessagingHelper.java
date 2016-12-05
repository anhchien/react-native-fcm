//Credits to react-native-push-notification

package com.evollu.react.fcm;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.SharedPreferences;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FIRLocalMessagingHelper {
    private static final long DEFAULT_VIBRATION = 300L;
    private static final String TAG = FIRLocalMessagingHelper.class.getSimpleName();
    private final static String PREFERENCES_KEY = "ReactNativeSystemNotification";
    private static boolean mIsForeground = false; //this is a hack

    private Context mContext;
    private SharedPreferences sharedPreferences = null;

    public FIRLocalMessagingHelper(Application context) {
        mContext = context;
        sharedPreferences = (SharedPreferences) mContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public Class getMainActivityClass() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public static Bundle toBundle(ReadableMap map) {
        Bundle bundle = new Bundle();

        if(map.hasKey("body"))
            bundle.putString("body", map.getString("body"));

        if(map.hasKey("title"))
            bundle.putString("title", map.getString("title"));

        if(map.hasKey("ticker"))
            bundle.putString("ticker", map.getString("ticker"));

        if(map.hasKey("number"))
            bundle.putInt("number", map.getInt("number"));

        if(map.hasKey("auto_cancel"))
            bundle.putBoolean("auto_cancel", map.getBoolean("auto_cancel"));

        if(map.hasKey("sub_text"))
            bundle.putString("sub_text", map.getString("sub_text"));

        if(map.hasKey("group"))
            bundle.putString("group", map.getString("group"));

        if(map.hasKey("data") && map.getType("data") == ReadableType.Map)
            bundle.putBundle("data", Arguments.toBundle(map.getMap("data")));

        if(map.hasKey("priority"))
            bundle.putString("priority", map.getString("priority"));

        if(map.hasKey("icon"))
            bundle.putString("icon", map.getString("icon"));

        if(map.hasKey("large_icon"))
            bundle.putString("large_icon", map.getString("large_icon"));

        if(map.hasKey("big_text"))
            bundle.putString("big_text", map.getString("big_text"));

        if(map.hasKey("sound"))
            bundle.putString("sound", map.getString("sound"));

        if(map.hasKey("color"))
            bundle.putString("color", map.getString("color"));

        if(map.hasKey("vibrate"))
            bundle.putLong("vibrate", (new Double(map.getDouble("vibrate"))).longValue());

        if(map.hasKey("lights"))
            bundle.putBoolean("lights", map.getBoolean("lights"));

        if(map.hasKey("show_in_foreground"))
            bundle.putBoolean("show_in_foreground", map.getBoolean("show_in_foreground"));

        if(map.hasKey("click_action"))
            bundle.putString("click_action", map.getString("click_action"));

        if(map.hasKey("id"))
            bundle.putString("id", map.getString("id"));

        if(map.hasKey("fire_date"))
            bundle.putLong("fire_date", (new Double(map.getDouble("fire_date"))).longValue());

        if(map.hasKey("repeat_interval"))
            bundle.putString("repeat_interval", map.getString("repeat_interval"));

        return bundle;
    }

    public void sendNotification(Bundle bundle) {
        try {
            Log.d(TAG, "1");
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                return;
            }

            Log.d(TAG, "2");
            if (bundle.getString("body") == null) {
                return;
            }
            Log.d(TAG, "3");
            Resources res = mContext.getResources();
            String packageName = mContext.getPackageName();

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = mContext.getApplicationInfo();
                title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
            }
            Log.d(TAG, "4");
            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                    .setContentTitle(title)
                    .setContentText(bundle.getString("body"))
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(bundle.getBoolean("auto_cancel", true))
                    .setNumber(bundle.getInt("number", 0))
                    .setSubText(bundle.getString("sub_text"))
                    .setGroup(bundle.getString("group"))
                    .setVibrate(new long[]{0, DEFAULT_VIBRATION})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setExtras(bundle.getBundle("data"));

            //priority
            String priority = bundle.getString("priority", "");
            switch(priority) {
                case "min":
                    notification.setPriority(NotificationCompat.PRIORITY_MIN);
                break;
                case "high":
                    notification.setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
                case "max":
                    notification.setPriority(NotificationCompat.PRIORITY_MAX);
                break;
                default:
                    notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            }

            //icon
            String smallIcon = bundle.getString("icon", "ic_launcher");
            int smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
            notification.setSmallIcon(smallIconResId);

            //large icon
            String largeIcon = bundle.getString("large_icon");
            if(largeIcon != null){
                int largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
                Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

                if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                    notification.setLargeIcon(largeIconBitmap);
                }
            }

            //big text
            String bigText = bundle.getString("big_text");
            if(bigText != null){
                notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
            }

            //sound
            if (bundle.containsKey("sound")) {
                if(bundle.getString("sound").equals("default")) {
                    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    notification.setSound(uri);
                } else {
                    try {
                        int soundResourceId = res.getIdentifier(bundle.getString("sound"), "raw", packageName);
                        notification.setSound(Uri.parse("android.resource://" + packageName + "/" + soundResourceId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                }
            }

            //vibrate
            if(bundle.containsKey("vibrate")){
                long vibrate = bundle.getLong("vibrate", 0);
                if(vibrate > 0){
                    notification.setVibrate(new long[]{0, vibrate});
                }else{
                    notification.setVibrate(null);
                }
            }

            //lights
            if (bundle.getBoolean("lights")) {
                notification.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
            }

            Log.d(TAG, "broadcast intent before showing notification");
            Intent i = new Intent("com.evollu.react.fcm.ReceiveLocalNotification");
            i.putExtras(bundle);
            mContext.sendOrderedBroadcast(i, null);

            if(!mIsForeground || bundle.getBoolean("show_in_foreground", true)){
                Intent intent = new Intent(mContext, intentClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtras(bundle);
                intent.setAction(bundle.getString("click_action"));

                int notificationID = bundle.containsKey("id") ? bundle.getString("id", "").hashCode() : (int) System.currentTimeMillis();
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager =
                        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                notification.setContentIntent(pendingIntent);

                Notification info = notification.build();

                if (bundle.containsKey("tag")) {
                    String tag = bundle.getString("tag");
                    notificationManager.notify(tag, notificationID, info);
                } else {
                    notificationManager.notify(notificationID, info);
                }
            }
            //clear out one time scheduled notification once fired
            if(!bundle.containsKey("repeat_interval") && bundle.containsKey("fire_date")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(bundle.getString("id"));
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to send local notification", e);
        }
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        String notificationId = bundle.getString("id");
        if(notificationId == null){
            Log.e(TAG, "failed to schedule notification because id is missing");
            return;
        }

        Long fireDate = bundle.getLong("fire_date", Math.round(bundle.getDouble("fire_date")));
        if (fireDate == 0) {
            Log.e(TAG, "failed to schedule notification because fire date is missing");
            return;
        }

        Intent notificationIntent = new Intent(mContext, FIRLocalMessagingPublisher.class);
        notificationIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notificationId.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Long interval = null;
        switch (bundle.getString("repeat_interval", "")) {
          case "minute":
              interval = (long) 60000;
              break;
          case "hour":
              interval = AlarmManager.INTERVAL_HOUR;
              break;
          case "day":
              interval = AlarmManager.INTERVAL_DAY;
              break;
          case "week":
              interval = AlarmManager.INTERVAL_DAY * 7;
              break;
        }

        if(interval != null){
            getAlarmManager().setRepeating(AlarmManager.RTC_WAKEUP, fireDate, interval, pendingIntent);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }

        //store intent
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            JSONObject json = BundleJSONConverter.convertToJSON(bundle);
            editor.putString(notificationId, json.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelLocalNotification(String notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(notificationId.hashCode());

        cancelAlarm(notificationId);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(notificationId);
        editor.apply();
    }

    public void cancelAllLocalNotifications() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        java.util.Map<String, ?> keyMap = sharedPreferences.getAll();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(java.util.Map.Entry<String, ?> entry:keyMap.entrySet()){
            cancelAlarm(entry.getKey());
        }
        editor.clear();
        editor.apply();
    }

    public void cancelAlarm(String notificationId) {
        Intent notificationIntent = new Intent(mContext, FIRLocalMessagingPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notificationId.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager().cancel(pendingIntent);
    }

    public ArrayList<Bundle> getScheduledLocalNotifications(){
        ArrayList<Bundle> array = new ArrayList<Bundle>();
        java.util.Map<String, ?> keyMap = sharedPreferences.getAll();
        for(java.util.Map.Entry<String, ?> entry:keyMap.entrySet()){
            try {
                JSONObject json = new JSONObject((String)entry.getValue());
                Bundle bundle = BundleJSONConverter.convertToBundle(json);
                array.add(bundle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    public void setApplicationForeground(boolean foreground){
        mIsForeground = foreground;
    }
}
