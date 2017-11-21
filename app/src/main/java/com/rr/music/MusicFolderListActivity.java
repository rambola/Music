package com.rr.music;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.rr.music.adapters.FolderMusicListAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.utils.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class MusicFolderListActivity extends AppCompatActivity {
    private String mFolderName = "";
    private String mAlbumArtPath = "";
    private final String LOG_TAG = MusicFolderListActivity.class.getSimpleName();
    private List<MusicDataModel> mMusicDataModels = new ArrayList<>();

    private FolderMusicListAdapter mFolderMusicListAdapter;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_folder_list);

        initViews();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(MusicFolderListActivity.this));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setItemAnimator(new FadeInAnimator());
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mFolderMusicListAdapter);
        alphaAdapter.setFirstOnly(true);
        alphaAdapter.setDuration(1500);
        alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
        mRecyclerView.setAdapter(alphaAdapter);

        mFolderMusicListAdapter.setOnItemClickListener(new FolderMusicListAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Log.d(LOG_TAG, "onItemClick(), position: "+position);
            }

            @Override
            public void onItemLongClick(int position, View v) {
                Log.d(LOG_TAG, "onItemLongClick(), position: "+position);
//                    showDeleteDialog(itemPositionInFullList, mMusicDataModels.get(
//                            itemPositionInFullList).getSongId(), mMusicDataModels.get(
//                            itemPositionInFullList).getSongDisplayName(),
//                            mMusicDataModels.get(itemPositionInFullList).getSongData());
            }
        });
    }

    private void initViews() {
        mFolderName = getIntent().getStringExtra(Utilities.INTENT_KEY_FOLDER_NAME);
        mAlbumArtPath = getIntent().getStringExtra(Utilities.INTENT_KEY_ALBUM_ART_PATH);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.musicFolderToolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(mFolderName);

        mRecyclerView = (RecyclerView) findViewById(R.id.musicFolderRecyclerView);

        mMusicDataModels = new MyMusicDB(MusicFolderListActivity.this).
                getSongsAlphabeticalOrderForFolder(mFolderName);
        Log.d(LOG_TAG, "mMusicDataModels.size(): "+mMusicDataModels.size());
        mFolderMusicListAdapter = new FolderMusicListAdapter(mMusicDataModels);
    }

}