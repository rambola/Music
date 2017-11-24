package com.rr.music.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.rr.music.Dashboard;
import com.rr.music.R;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = MyWidgetProvider.class.getName();
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "..........onReceive()...........");
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        mContext = context;
        Log.d(LOG_TAG, "..........onEnabled()...........");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d(LOG_TAG, "..........onUpdate()...........");

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            // Set the text
//            remoteViews.setTextViewText(R.id.update, String.valueOf(number));
            Intent intent = new Intent(context, Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pending = PendingIntent.getActivity(context, 0,intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.musicCoverImg, pending);
            // Register an onClickListener
//            Intent intent = new Intent(context, MyWidgetProvider.class);
//
//            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

}