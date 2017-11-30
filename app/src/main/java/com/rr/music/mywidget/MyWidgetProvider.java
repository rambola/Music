package com.rr.music.mywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.rr.music.ActivityForWidget;
import com.rr.music.R;
import com.rr.music.background.WidgetMusicPlayerService;
import com.rr.music.database.MyMusicDB;
import com.rr.music.datamodels.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = MyWidgetProvider.class.getName();

    private WidgetMusicPlayerService widgetMusicPlayerService;
    private Intent playIntent;

    private List<MusicDataModel> mSongsList = new ArrayList<>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d(LOG_TAG, "..........onUpdate()...........");

        final int INTENT_FLAGS = 0;
        final int REQUEST_CODE = 1;

        ComponentName thisWidget = new ComponentName(context,
                MyWidgetProvider.class);
        // Get all ids
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            Intent intent = new Intent(context, ActivityForWidget.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetMusicCoverImg, pending);

            Intent shuffleIntent = new Intent(context, WidgetMusicPlayerService.class);
            shuffleIntent.putExtra(Utilities.WIDGET_INTENT_FOR_KEY, Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY);
            PendingIntent shufflePendingIntent = PendingIntent.getService(
                    context, 1, shuffleIntent, INTENT_FLAGS);

            Intent prevIntent = new Intent(context, WidgetMusicPlayerService.class);
            prevIntent.putExtra(Utilities.WIDGET_INTENT_FOR_KEY, Utilities.WIDGET_INTENT_FOR_PREVIOUS_KEY);
            PendingIntent prevPendingIntent = PendingIntent.getService(
                    context, 2, prevIntent, INTENT_FLAGS);

            Intent playPauseIntent = new Intent(context, WidgetMusicPlayerService.class);
            playPauseIntent.putExtra(Utilities.WIDGET_INTENT_FOR_KEY, Utilities.WIDGET_INTENT_FOR_PLAY_PAUSE_KEY);
            PendingIntent playPausePendingIntent = PendingIntent.getService(
                    context, 3, playPauseIntent, INTENT_FLAGS);

            Intent nextIntent = new Intent(context, WidgetMusicPlayerService.class);
            nextIntent.putExtra(Utilities.WIDGET_INTENT_FOR_KEY, Utilities.WIDGET_INTENT_FOR_NEXT_KEY);
            PendingIntent nextPendingIntent = PendingIntent.getService(
                    context, 4, nextIntent, INTENT_FLAGS);

            Intent repeatIntent = new Intent(context, WidgetMusicPlayerService.class);
            repeatIntent.putExtra(Utilities.WIDGET_INTENT_FOR_KEY, Utilities.WIDGET_INTENT_FOR_REPEAT_KEY);
            PendingIntent repeatPendingIntent = PendingIntent.getService(
                    context, 5, repeatIntent, INTENT_FLAGS);

            remoteViews.setOnClickPendingIntent(R.id.widgetShuffleIV, shufflePendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widgetPrevSongIV, prevPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widgetPlayPauseSongIV, playPausePendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widgetNextSongIV, nextPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widgetRepeatSongIV, repeatPendingIntent);

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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "..........onReceive()..........."+intent.getAction());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);

        if(Utilities.ACTION_UPDATE_MY_WIDGET.equalsIgnoreCase(intent.getAction())) {
            boolean playFromFolder = intent.getBooleanExtra(
                    Utilities.WIDGET_INTENT_KEY_PLAY_FROM_FOLDER, false);
            int startPlayPosition = intent.getIntExtra(
                    Utilities.WIDGET_INTENT_KEY_START_PLAY_POSITION, 0);
            String folderName = intent.getStringExtra(Utilities.WIDGET_INTENT_KEY_FOLDER_NAME);
            Log.d(LOG_TAG, "playFromFolder: "+playFromFolder+", startPlayPosition: "+
                    startPlayPosition+", folderName: "+folderName);

            mSongsList = getSongsList(context, playFromFolder, folderName);

            if(null != mSongsList && mSongsList.size() > 0) {
                String imageUri = mSongsList.get(startPlayPosition).getAlbumArtPath();
                Log.d(LOG_TAG, "imageUri: "+imageUri);

//                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//                ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);

                remoteViews.setTextViewText(R.id.widgetSongNameTV, mSongsList.get(
                        startPlayPosition).getSongDisplayName());
                remoteViews.setTextViewText(R.id.widgetSongDetailsTV, mSongsList.get(
                        startPlayPosition).getSongAlbum());
                if(null != imageUri)
                    remoteViews.setImageViewUri(R.id.widgetMusicCoverImg, Uri.parse(imageUri));
                else
                    remoteViews.setImageViewResource(R.id.widgetMusicCoverImg, R.mipmap.app_icon);

                appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                widgetMusicPlayerService.setList(mSongsList);
                widgetMusicPlayerService.playSong();
            }
        } else if(Utilities.ACTION_UPDATE_MY_WIDGET_FROM_SERVICE.equalsIgnoreCase(intent.getAction())) {
            String songName = intent.getStringExtra(Utilities.WIDGET_INTENT_KEY_SONG_NAME_FROM_SERVICE);
            String albumName = intent.getStringExtra(Utilities.WIDGET_INTENT_KEY_ALBUM_NAME_FROM_SERVICE);
            String imageUri = intent.getStringExtra(Utilities.WIDGET_INTENT_KEY_IMAGE_URI_FROM_SERVICE);
            Log.d(LOG_TAG, "songName: "+songName+", albumName: "+albumName+", imageUri: "+imageUri);

            remoteViews.setTextViewText(R.id.widgetSongNameTV, songName);
            remoteViews.setTextViewText(R.id.widgetSongDetailsTV, albumName);

            if(null != imageUri)
                remoteViews.setImageViewUri(R.id.widgetMusicCoverImg, Uri.parse(imageUri));
            else
                remoteViews.setImageViewResource(R.id.widgetMusicCoverImg, R.mipmap.app_icon);

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        } else if(Utilities.ACTION_APPWIDGET_UPDATE.equalsIgnoreCase(intent.getAction())) {
            if(null == playIntent){
                widgetMusicPlayerService = new WidgetMusicPlayerService();
                widgetMusicPlayerService.setList(mSongsList);

                playIntent = new Intent(context, WidgetMusicPlayerService.class);
//                context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                context.startService(playIntent);
            }
        } else if(Utilities.ACTION_WIDGET_GREY.equalsIgnoreCase(intent.getAction())) {
            String whichIcon = intent.getStringExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON);
            Log.d(LOG_TAG, "whichIcon: "+whichIcon);

            if(whichIcon.equalsIgnoreCase(Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY)) {
                remoteViews.setImageViewResource(R.id.widgetShuffleIV, R.mipmap.white_shuffle);
            } else if(whichIcon.equalsIgnoreCase(Utilities.WIDGET_INTENT_FOR_REPEAT_KEY)) {
                remoteViews.setImageViewResource(R.id.widgetRepeatSongIV, R.mipmap.white_repeat);
            }
        } else if(Utilities.ACTION_WIDGET_ORANGE.equalsIgnoreCase(intent.getAction())) {
            String whichIcon = intent.getStringExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON);
            Log.d(LOG_TAG, "whichIcon: "+whichIcon);

            if(whichIcon.equalsIgnoreCase(Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY)) {
                remoteViews.setImageViewResource(R.id.widgetShuffleIV, R.mipmap.orange_shuffle);
            } else if(whichIcon.equalsIgnoreCase(Utilities.WIDGET_INTENT_FOR_REPEAT_KEY)) {
                remoteViews.setImageViewResource(R.id.widgetRepeatSongIV, R.mipmap.orange_repeat);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "..........onDeleted()...........playIntent: "+playIntent+
                ", widgetMusicPlayerService: "+widgetMusicPlayerService);

        if(null != playIntent)
            context.stopService(playIntent);

        if(null != widgetMusicPlayerService) {
            widgetMusicPlayerService.stopSelf();
            widgetMusicPlayerService.stopForeground(true);
        }
    }

    /*//connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WidgetMusicPlayerService.MusicBinder binder = (WidgetMusicPlayerService.MusicBinder)service;
            //get service
            widgetMusicPlayerService = binder.getService();
            //pass list
            widgetMusicPlayerService.setList(mSongsList);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };*/


    private List<MusicDataModel> getSongsList(Context context, boolean playFromFolder, String folderName) {
        List<MusicDataModel> musicDataModels;

        if(playFromFolder)
            musicDataModels = new MyMusicDB(context).getSongsAlphabeticalOrderForFolder(folderName);
        else
            musicDataModels = new MyMusicDB(context).getSongsAlphabeticalOrder();

        Log.d(LOG_TAG, "getSongsList(), before returning, musicDataModels.size(): "+musicDataModels.size());
        return musicDataModels;
    }

}