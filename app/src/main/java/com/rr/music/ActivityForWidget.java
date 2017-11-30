package com.rr.music;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.rr.music.adapters.FolderMusicAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class ActivityForWidget extends AppCompatActivity {
    private final String LOG_TAG = ActivityForWidget.class.getSimpleName();
    private ArrayList<HashMap<String, String>> mHashMapList = new ArrayList<>();

    FolderMusicAdapter mFolderMusicAdapter;

    RelativeLayout mOptionsRelativeLayout;
    Button mPlayAllBtn;
    Button mPlayFromFolderBtn;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_widget);

        initViews();

        mPlayAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "....mPlayAllBtn.setOnClickListener....");
                Intent intent = new Intent();
                intent.setAction(Utilities.ACTION_UPDATE_MY_WIDGET);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_PLAY_FROM_FOLDER, false);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_START_PLAY_POSITION, 0);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_FOLDER_NAME, "");
                sendBroadcast(intent);
                finish();
            }
        });

        mPlayFromFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "....mPlayFromFolderBtn.setOnClickListener....");
                mOptionsRelativeLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

                mHashMapList = new MyMusicDB(ActivityForWidget.this).getFolderNamesWithMusicImage();
                Log.d(LOG_TAG, "hashMapList.size(): " + mHashMapList.size());

                mFolderMusicAdapter = new FolderMusicAdapter(mHashMapList);

                mRecyclerView.setItemAnimator(new FadeInAnimator());
                AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mFolderMusicAdapter);
                alphaAdapter.setFirstOnly(true);
                alphaAdapter.setDuration(1000);
                alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
                mRecyclerView.setAdapter(alphaAdapter);
            }
        });

        mFolderMusicAdapter.setOnItemClickListener(new FolderMusicAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, String folderName, View view) {
                Log.d(LOG_TAG, "onItemClick(), position: "+position+", folderName: "+folderName);

                Intent intent = new Intent();
                intent.setAction(Utilities.ACTION_UPDATE_MY_WIDGET);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_PLAY_FROM_FOLDER, true);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_START_PLAY_POSITION, position);
                intent.putExtra(Utilities.WIDGET_INTENT_KEY_FOLDER_NAME, folderName);
                sendBroadcast(intent);
                finish();
            }
        });
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOptionsRelativeLayout = (RelativeLayout) findViewById(R.id.widgetActivityOptionsLayout);
        mPlayAllBtn = (Button) findViewById(R.id.forWidgetPlayAllBtn);
        mPlayFromFolderBtn = (Button) findViewById(R.id.forWidgetPlayFromFolderBtn);
        mRecyclerView = (RecyclerView) findViewById(R.id.widgetActivityRecyclerView);
    }

}