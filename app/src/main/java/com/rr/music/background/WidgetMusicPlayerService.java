package com.rr.music.background;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.rr.music.R;
import com.rr.music.datamodels.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.List;
import java.util.Random;

public class WidgetMusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = WidgetMusicPlayerService.class.getName();
    private List<MusicDataModel> mSongs;
    private String mSongTitle="";
    private int mSongPosition = 0;
    private static final int NOTIFY_ID = 1;
    private boolean mShuffle = false;
    private boolean mPlayRepeat = false;
    private Random mRand;

    private MediaPlayer mMediaPlayer;
    private final IBinder mMusicBind = new MusicBinder();


    public void onCreate(){
        super.onCreate();

        Log.i(LOG_TAG, "......onCreate().....");

        mSongPosition = -1;
        mRand = new Random();
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "......onStartCommand().....intent: "+intent);
        if(null != intent) {
            String widgetIntentForKey = intent.getStringExtra(Utilities.WIDGET_INTENT_FOR_KEY);
            Log.d(LOG_TAG, "......onStartCommand().....widgetIntentForKey: " + widgetIntentForKey
                    +", mSongPosition: "+mSongPosition);

            if(Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY.equalsIgnoreCase(widgetIntentForKey)
                    && mSongPosition != -1)
                setShuffle();
            else if(Utilities.WIDGET_INTENT_FOR_PREVIOUS_KEY.equalsIgnoreCase(widgetIntentForKey)
                    && mSongPosition != -1)
                playPrev();
            else if(Utilities.WIDGET_INTENT_FOR_PLAY_PAUSE_KEY.equalsIgnoreCase(widgetIntentForKey)
                    && mSongPosition != -1) {
                if(isPlaying())
                    pausePlayer();
                else
                    startPlayer();
            } else if(Utilities.WIDGET_INTENT_FOR_NEXT_KEY.equalsIgnoreCase(widgetIntentForKey)
                    && mSongPosition != -1)
                playNext();
            else if(Utilities.WIDGET_INTENT_FOR_REPEAT_KEY.equalsIgnoreCase(widgetIntentForKey)
                    && mSongPosition != -1)
                playRepeat();
        }

        return START_STICKY;
    }

    public void initMusicPlayer(){
        //set player properties
//        mMediaPlayer.setWakeMode(getApplicationContext(),
//                PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    //binder
    public class MusicBinder extends Binder {
        public WidgetMusicPlayerService getService() {
            return WidgetMusicPlayerService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //check if playback has reached the end of a track
        if(mMediaPlayer.getCurrentPosition()>0){
            mp.reset();
            if(!mPlayRepeat)
                playNext();
            else
                playSame();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        //notification
//        Intent notIntent = new Intent(this, Dashboard.class);
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
//                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder/*.setContentIntent(pendInt)*/
                .setSmallIcon(R.mipmap.app_icon)
                .setTicker(mSongTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(mSongTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //pass song list
    public void setList(List<MusicDataModel> theSongs){
        mSongs = theSongs;
    }

    //play a song
    public void playSong(){
        mMediaPlayer.reset();

        MusicDataModel playSong = mSongs.get(mSongPosition);
        mSongTitle = playSong.getSongDisplayName();
        Uri trackUri = Uri.parse(playSong.getSongData());
        try{
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();
    }

    public void playSame() {
        playSong();
    }

    public void playPrev(){
        mSongPosition--;
        if(mSongPosition < 0) mSongPosition = mSongs.size()-1;
        playSong();
    }

    public void playNext(){
        if(mShuffle){
            int newSong = mSongPosition;
            while(newSong == mSongPosition){
                newSong = mRand.nextInt(mSongs.size());
            }
            mSongPosition = newSong;
        }
        else{
            mSongPosition++;
            if(mSongPosition >= mSongs.size())
                mSongPosition = 0;
        }

        MusicDataModel playSong = mSongs.get(mSongPosition);

        Intent intent = new Intent();
        intent.setAction(Utilities.ACTION_UPDATE_MY_WIDGET_FROM_SERVICE);
        intent.putExtra(Utilities.WIDGET_INTENT_KEY_SONG_NAME_FROM_SERVICE, playSong.getSongDisplayName());
        intent.putExtra(Utilities.WIDGET_INTENT_KEY_ALBUM_NAME_FROM_SERVICE, playSong.getSongAlbum());
        intent.putExtra(Utilities.WIDGET_INTENT_KEY_IMAGE_URI_FROM_SERVICE, playSong.getAlbumArtPath());
        sendBroadcast(intent);

        playSong();
    }

    public void setShuffle(){
        mShuffle = !mShuffle;
        if(mShuffle) {
            Intent intent = new Intent();
            intent.setAction(Utilities.ACTION_WIDGET_ORANGE);
            intent.putExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON,
                    Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(Utilities.ACTION_WIDGET_GREY);
            intent.putExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON,
                    Utilities.WIDGET_INTENT_FOR_SHUFFLE_KEY);
            sendBroadcast(intent);
        }
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mMediaPlayer.pause();
    }

    public void startPlayer(){
        mMediaPlayer.start();
    }

    public void playRepeat() {
        mPlayRepeat = !mPlayRepeat;
        if(mPlayRepeat) {
            Intent intent = new Intent();
            intent.setAction(Utilities.ACTION_WIDGET_ORANGE);
            intent.putExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON,
                    Utilities.WIDGET_INTENT_FOR_REPEAT_KEY);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(Utilities.ACTION_WIDGET_GREY);
            intent.putExtra(Utilities.WIDGET_INTENT_FOR_WHICH_ICON,
                    Utilities.WIDGET_INTENT_FOR_REPEAT_KEY);
            sendBroadcast(intent);
        }
    }

    //set the song
    public void setSong(int songIndex){
        mSongPosition = songIndex;
    }

    public int getPosn(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mMediaPlayer.getDuration();
    }

    public void seek(int posn){
        mMediaPlayer.seekTo(posn);
    }

}