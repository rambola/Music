package com.rr.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rr.music.R;
import com.rr.music.adapters.FragmentsViewPagerAdapter;
import com.rr.music.database.MyMusicDB;

public class MediaMusicStoreTask extends AsyncTask<Void, Void, Void> {
    private final String LOG_TAG = MediaMusicStoreTask.class.getSimpleName();
    private Context mContext;
    private ContentLoadingProgressBar mContentLoadingProgressBar;
    private FragmentsViewPagerAdapter mFragmentsViewPagerAdapter;

    public MediaMusicStoreTask(Context context, View view, FragmentsViewPagerAdapter
            fragmentsViewPagerAdapter) {
        this.mContext = context;
        mContentLoadingProgressBar = (ContentLoadingProgressBar) view;
        mFragmentsViewPagerAdapter = fragmentsViewPagerAdapter;
    }

    private String getFolderName (String songPath) {
        String[] pathSplit = songPath.split("/");

        if(pathSplit.length > 5) {
            String onlyPath = songPath.substring(0, songPath.lastIndexOf("/"));
            return onlyPath.substring(onlyPath.lastIndexOf("/") + 1, onlyPath.length()).trim();
        }
        else {
            return "OnlyMusicFiles";
        }
    }

    private String getAlbumArtPath(String albumId) {
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?", new String[]{albumId}, null);

        String path = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                cursor.close();
            }
        }

        return path;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mContentLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int musicCount = 0;
        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String orderBy = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, orderBy);

        if(null != cursor && cursor.getCount() > 0) {
            Log.d(LOG_TAG, "getAllMusicFiles(), cursor.getCount(): "+cursor.getCount());

            while (cursor.moveToNext()) {
                if (musicCount == 100) {
                    Log.d(LOG_TAG, "getAllMusicFiles(), musicCount: "+musicCount);
                    musicCount = 0;
                    publishProgress();
                }

                String folderName = getFolderName(cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                String albumArtPath = getAlbumArtPath(cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));

                new MyMusicDB(mContext).insertSongDetails(cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media._ID)), cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)), albumArtPath,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        folderName);

                musicCount++;
            }

            if (musicCount <= 100) {
                publishProgress();
            }

            Log.d(LOG_TAG, "doInBackground(), done getting media files from media store.");
            cursor.close();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        Log.d(LOG_TAG, "onProgressUpdate is called");

        mContentLoadingProgressBar.setVisibility(View.GONE);
        mFragmentsViewPagerAdapter.updateAlphabeticalFragment();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(mContext, mContext.getString(R.string.allSetNow), Toast.LENGTH_LONG).show();
    }

}