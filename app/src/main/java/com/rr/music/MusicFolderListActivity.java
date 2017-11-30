package com.rr.music;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rr.music.adapters.MusicFolderListAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.datamodels.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class MusicFolderListActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    private String mFolderName = "";
    private String mAlbumArtPath = "";
    private final String LOG_TAG = MusicFolderListActivity.class.getSimpleName();
    private List<MusicDataModel> mMusicDataModels = new ArrayList<>();
    private int mClickedPosition = -1;

    private MusicFolderListAdapter mMusicFolderListAdapter;

    private MediaPlayer mMediaPlayer;
    private ImageView imageView;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_folder_list);

        initViews();

        Glide.with(MusicFolderListActivity.this).load(Uri.parse(mAlbumArtPath))/*.centerCrop()*/.
                diskCacheStrategy(DiskCacheStrategy.ALL)./*placeholder(R.mipmap.app_icon).
                transform(new GlideCircleTransform(MusicFolderListActivity.this)).*/
                into(imageView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(MusicFolderListActivity.this));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setItemAnimator(new FadeInAnimator());
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mMusicFolderListAdapter);
        alphaAdapter.setFirstOnly(true);
        alphaAdapter.setDuration(1500);
        alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
        mRecyclerView.setAdapter(alphaAdapter);

        mMusicFolderListAdapter.setOnItemClickListener(new MusicFolderListAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Log.d(LOG_TAG, "onItemClick(), position: "+position);
//                stopOtherMusic();
                playMusic(mMusicDataModels, position);
            }

            @Override
            public void onItemLongClick(int position, View v) {
                Log.d(LOG_TAG, "onItemLongClick(), position: "+position);
                showDeleteDialog(position, mMusicDataModels.get(
                        position).getSongId(), mMusicDataModels.get(
                        position).getSongDisplayName(),
                        mMusicDataModels.get(position).getSongData());
            }
        });

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mMediaPlayer) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mFloatingActionButton.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        if (-1 == mClickedPosition) {
                            if(null != mMusicDataModels && mMusicDataModels.size() > 0)
                                playMusic(mMusicDataModels, 0);
                        } else {
                            mMediaPlayer.start();
                            mFloatingActionButton.setImageResource(android.R.drawable.ic_media_pause);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicFolderListActivity.class.getName());
        LocalBroadcastManager.getInstance(MusicFolderListActivity.this).registerReceiver(
                broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if(null != mRecyclerView)
            mRecyclerView.clearOnScrollListeners();

        if(null != mMusicFolderListAdapter)
            mMusicFolderListAdapter.setOnItemClickListener(null);

        if(null != broadcastReceiver)
            LocalBroadcastManager.getInstance(MusicFolderListActivity.this).unregisterReceiver(
                    broadcastReceiver);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mFloatingActionButton.setImageResource(android.R.drawable.ic_media_play);
        if(mClickedPosition < mMusicDataModels.size())
            playMusic(mMusicDataModels, mClickedPosition + 1);
        else
            playMusic(mMusicDataModels, 0);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive(), intent.getAction(): "+intent.getAction());
            if(null != mMusicDataModels)
                mMusicDataModels.clear();

            mMusicDataModels = new MyMusicDB(MusicFolderListActivity.this).
                    getSongsAlphabeticalOrderForFolder(mFolderName);

            mMusicFolderListAdapter.updateAdapter(mMusicDataModels);
        }
    };

    private void initViews() {
        mFolderName = getIntent().getStringExtra(Utilities.INTENT_KEY_FOLDER_NAME);
        mAlbumArtPath = getIntent().getStringExtra(Utilities.INTENT_KEY_ALBUM_ART_PATH);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.musicFolderToolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(mFolderName);

        imageView = (ImageView) findViewById(R.id.musicFolderImageIV);
        mRecyclerView = (RecyclerView) findViewById(R.id.musicFolderRecyclerView);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);

        mMusicDataModels = new MyMusicDB(MusicFolderListActivity.this).
                getSongsAlphabeticalOrderForFolder(mFolderName);
        Log.d(LOG_TAG, "mMusicDataModels.size(): "+mMusicDataModels.size());
        mMusicFolderListAdapter = new MusicFolderListAdapter(mMusicDataModels);
    }

    private void playMusic (List<MusicDataModel> musicDataModels, int position) {
        if(Dashboard.mDashboard.isMusicPlaying())
            Dashboard.mDashboard.stopPlaying();

        if(null != mMediaPlayer) {
            mClickedPosition = position;
            MusicDataModel musicDataModel = musicDataModels.get(position);

            mFloatingActionButton.setImageResource(android.R.drawable.ic_media_pause);

            if(mMediaPlayer.isPlaying())
                mMediaPlayer.stop();

            try {
                Log.d(LOG_TAG, "path: "+musicDataModel.getSongData());
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(musicDataModel.getSongData());
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                mMusicFolderListAdapter.newRowIndex(position);
            } catch (IOException e) {
                e.printStackTrace();
                mFloatingActionButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    private void showDeleteDialog(final int position, final String songId, final String songDisplayName,
                                  final String songPath) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MusicFolderListActivity.this);
        alertDialog.setTitle(songDisplayName);
        alertDialog.setPositiveButton(getString(R.string.delete), null);
        alertDialog.setNegativeButton(getString(R.string.rename), null);
        alertDialog.setNeutralButton(getString(R.string.update), null);
        alertDialog.setMessage("You can Re-name or Delete the song.");

        final EditText input = new EditText(MusicFolderListActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        input.setVisibility(View.GONE);

        final AlertDialog dialog = alertDialog.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isFileDeleted = deleteFileFromMediaStore(songId);

                        Log.d(LOG_TAG, "showDeleteDialog(), after deleting, " +
                                "isFileDeleted: " + isFileDeleted);
                        if (isFileDeleted) {
                            mMusicDataModels.remove(position);
                            mMusicFolderListAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MusicFolderListActivity.this, getString(R.string.failedDelete),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        input.setVisibility(View.VISIBLE);
                        input.setText(songDisplayName);

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).
                                setVisibility(View.INVISIBLE);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).
                                setVisibility(View.VISIBLE);
                    }
                });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newName = input.getText().toString().trim();

                        String folderPath = songPath.substring(0, songPath.lastIndexOf("/"));
                        Log.d(LOG_TAG, "showDeleteDialog(), updating folderPath: " + folderPath);
                        Log.d(LOG_TAG, "showDeleteDialog(), songPath: " + songPath);

                        if (!TextUtils.isEmpty(newName)) {
                            if (!newName.contains(".mp3"))
                                newName = newName + ".mp3";

                            boolean renamed = renameFileInMediaStore(songId, newName);

                            Log.d(LOG_TAG, "showDeleteDialog(), renamed: " + renamed);

                            if (renamed) {
                                MusicDataModel musicDataModel = mMusicDataModels.get(position);
                                musicDataModel.setSongDisplayName(newName);
                                mMusicFolderListAdapter.notifyDataSetChanged();

                                dialog.dismiss();
                            } else {
                                Toast.makeText(MusicFolderListActivity.this, getString(R.string.failedRename),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(MusicFolderListActivity.this, getString(R.string.nameEmtpy),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean deleteFileFromMediaStore(final String songId) {
        boolean deleted = false;

        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {songId};

        ContentResolver contentResolver = getContentResolver();
        int del = contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                selection, selectionArgs);

        Log.d(LOG_TAG, "deleteFileFromMediaStore(), del: " + del);
        if (del > 0)
            deleted = true;

        return deleted;
    }

    private boolean renameFileInMediaStore(final String songId, final String newName) {
        boolean renamed = false;

        String selection = MediaStore.Audio.Media._ID + " = ? ";
        String[] selectionArgs = {songId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, newName);

        ContentResolver contentResolver = getContentResolver();
        int update = contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues, selection, selectionArgs);
        Log.d(LOG_TAG, "renameFileInMediaStore(), update: " + update);
        if (update > 0)
            renamed = true;

        return renamed;
    }

//    private void stopOtherMusic () {
//        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//
//// Request audio focus for playback
//        int result = am.requestAudioFocus(focusChangeListener,
//// Use the music stream.
//                AudioManager.STREAM_MUSIC,
//// Request permanent focus.
//                AudioManager.AUDIOFOCUS_GAIN);
//
//
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//// other app had stopped playing song now , so u can do u stuff now .
//        }
//    }

    /*private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    AudioManager am =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                            // Lower the volume while ducking.
                            mMediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            mMediaPlayer.pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            mMediaPlayer.stop();
                            ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            am.unregisterMediaButtonEventReceiver(component);
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            // Return the volume to normal and resume if paused.
                            mediaPlayer.setVolume(1f, 1f);
                            mediaPlayer.start();
                            break;
                        default: break;
                    }
                }
            };*/

}