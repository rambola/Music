package com.rr.music;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rr.music.adapters.FragmentsViewPagerAdapter;
import com.rr.music.utils.MediaMusicStoreTask;
import com.rr.music.utils.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    CardView mCardView;
    AppCompatSeekBar mAppCompatSeekBar;
    TextView mSongNameTV;
    TextView mSongDurationTV;
    ImageView mPlayPauseIV;
    MediaPlayer mMediaPlayer;
    ViewPager mViewPager;
    ContentLoadingProgressBar mContentLoadingProgressBar;
    FragmentsViewPagerAdapter mFragmentsAdapter;
    TabLayout mTabLayout;
    public static Dashboard mDashboard;

    private Handler mHandler = new Handler();
    MediaMusicStoreTask mMediaMusicStoreTask;

    private List<MusicDataModel> mMusicDataModelList = new ArrayList<>();
    private int clickedPosition = -1;
    private boolean isOpenedFromWidget = false;
    private final String LOG_TAG = Dashboard.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mDashboard = Dashboard.this;
        initializeViews();

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.alphabeticFragment)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.foldersFragment)));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragmentsAdapter = new FragmentsViewPagerAdapter
                (getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(mFragmentsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mPlayPauseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mMediaPlayer) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mPlayPauseIV.setImageResource(R.mipmap.play);
                    } else {
                        mMediaPlayer.start();
                        mPlayPauseIV.setImageResource(R.mipmap.pause);
                    }
                }
            }
        });

        mMediaMusicStoreTask = new MediaMusicStoreTask(Dashboard.this, mContentLoadingProgressBar,
                mFragmentsAdapter);
        mMediaMusicStoreTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDashboard = null;

        if(null != mViewPager)
            mViewPager.clearOnPageChangeListeners();

        if(null != mTabLayout)
            mTabLayout.clearOnTabSelectedListeners();

        if(null != mMediaMusicStoreTask && (AsyncTask.Status.PENDING ==
                mMediaMusicStoreTask.getStatus() || AsyncTask.Status.RUNNING ==
                mMediaMusicStoreTask.getStatus())) {
            mMediaMusicStoreTask.cancel(true);
        }

        if(null != mHandler && null != mUpdateTimeTask)
            mHandler.removeCallbacks(mUpdateTimeTask);

        if(null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if(null != mViewPager) {
            if(1 == mViewPager.getCurrentItem())
                mViewPager.setCurrentItem(0);
            else {
                super.onBackPressed();

                updateTheWidget();
            }
        } else {
            super.onBackPressed();

            updateTheWidget();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCardView = (CardView) findViewById(R.id.bottomLayout);
        mAppCompatSeekBar = (AppCompatSeekBar) findViewById(R.id.songSeekBar);
        mSongNameTV = (TextView) findViewById(R.id.songNameTV);
        mSongDurationTV = (TextView) findViewById(R.id.songDurationTV);
        mPlayPauseIV = (ImageView) findViewById(R.id.playPauseIV);
        mContentLoadingProgressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mAppCompatSeekBar.setOnSeekBarChangeListener(this);

        mCardView.setVisibility(View.GONE);

        isOpenedFromWidget = getIntent().getBooleanExtra(Utilities.IS_OPENED_FROM_WIDGET, false);
    }

    public void playMusic(List<MusicDataModel> musicDataModels, int position) {
        if(null != mMediaPlayer) {
            mMusicDataModelList = musicDataModels;
            clickedPosition = position;
            MusicDataModel musicDataModel = musicDataModels.get(position);

            if(View.GONE == mCardView.getVisibility())
                showBottomLayout();
            mPlayPauseIV.setImageResource(R.mipmap.pause);

            if(mMediaPlayer.isPlaying())
                mMediaPlayer.stop();

            try {
                mCardView.setVisibility(View.VISIBLE);

                Log.d(LOG_TAG, "displayName: "+musicDataModel.getSongDisplayName());
                mSongNameTV.setText(musicDataModel.getSongDisplayName());

                Log.d(LOG_TAG, "path: "+musicDataModel.getSongData());
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(musicDataModel.getSongData());
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                mAppCompatSeekBar.setProgress(0);
                mAppCompatSeekBar.setMax(100);

                updateProgressBar();
            } catch (IOException e) {
                e.printStackTrace();
                mPlayPauseIV.setImageResource(R.mipmap.play);
            }
        }
    }

    public boolean isMusicPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    public void stopPlaying() {
        if(null != mMediaPlayer) {
            mMediaPlayer.pause();
            mPlayPauseIV.setImageResource(R.mipmap.play);
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currentDuration = mMediaPlayer.getCurrentPosition();

            mSongDurationTV.setText(milliSecondsToTimer(currentDuration)+"-"+
                    milliSecondsToTimer(totalDuration));

            // Updating progress bar
            int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
            mAppCompatSeekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    public void showBottomLayout() {
        mCardView.setVisibility(View.VISIBLE);
    }

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(clickedPosition < mMusicDataModelList.size())
            playMusic(mMusicDataModelList, clickedPosition + 1);
        else
            playMusic(mMusicDataModelList, 0);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mMediaPlayer.getDuration();
        int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mMediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    private void updateTheWidget () {
        /*First check the app is opened from widget or not.
        If opened from widget then only update the widget*/

//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_2x1);
//        ComponentName thisWidget = new ComponentName(context, MyWidget.class);
//        remoteViews.setTextViewText(R.id.my_text_view, "myText" + System.currentTimeMillis());
//        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

}