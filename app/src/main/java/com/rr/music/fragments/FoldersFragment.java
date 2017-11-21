package com.rr.music.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.rr.music.MusicFolderListActivity;
import com.rr.music.R;
import com.rr.music.adapters.MusicAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.utils.ItemOffsetDecoration;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class FoldersFragment extends Fragment {
    private final String LOG_TAG = FoldersFragment.class.getSimpleName();
    private ArrayList<HashMap<String, String>> mHashMapList = new ArrayList<>();
    private Context mContext;
    private MusicAdapter mMusicAdapter;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(LOG_TAG, "isVisibleToUser: "+isVisibleToUser);

        if (isVisibleToUser) {
            if(null != mMusicAdapter && mMusicAdapter.getItemCount() < 1)
                updateRecyclerView();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (null != getView()) {
            RecyclerView mRecyclerView = getView().findViewById(R.id.fragmentsRecyclerView);

            if (null != getView()) {
                new MyMusicDB(mContext).clearTable();

                mRecyclerView = getView().findViewById(R.id.fragmentsRecyclerView);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,
                        Utilities.GRID_COLUMNS));
                ItemOffsetDecoration itemOffsetDecoration =
                        new ItemOffsetDecoration(mContext, R.dimen.item_offset);
                mRecyclerView.addItemDecoration(itemOffsetDecoration);

                if (null != mHashMapList)
                    mHashMapList.clear();

                mHashMapList = new MyMusicDB(mContext).getFolderNamesWithMusicImage();
                Log.d(LOG_TAG, "mHashMapList.size(): " + mHashMapList.size());

                mMusicAdapter = new MusicAdapter(mHashMapList, Utilities.FOLDERS);

                mRecyclerView.setItemAnimator(new FadeInAnimator());
                AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mMusicAdapter);
                alphaAdapter.setFirstOnly(true);
                alphaAdapter.setDuration(1500);
                alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
                mRecyclerView.setAdapter(alphaAdapter);

                itemClickListener();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void itemClickListener() {
        mMusicAdapter.setOnItemClickListener(new MusicAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, String folderName, View view) {
                Log.d(LOG_TAG, "onItemClick(), position: "+position+", folderName: "+folderName);
                Intent intent = new Intent(mContext, MusicFolderListActivity.class);
                intent.putExtra(Utilities.INTENT_KEY_FOLDER_NAME, folderName);
                intent.putExtra(Utilities.INTENT_KEY_ALBUM_ART_PATH, mHashMapList.get(position).
                        get(Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int position, String songDisplayName, View view) {
                Log.d(LOG_TAG, "onItemLongClick(), position: " + position);
            }
        });
    }

    private void updateRecyclerView () {
        if (null != mHashMapList)
            mHashMapList.clear();

        mHashMapList = new MyMusicDB(mContext).getFolderNamesWithMusicImage();
        Log.d(LOG_TAG, "hashMapList.size(): " + mHashMapList.size());

        mMusicAdapter.updateAdapter(mHashMapList, Utilities.FOLDERS);

        itemClickListener();
    }

}