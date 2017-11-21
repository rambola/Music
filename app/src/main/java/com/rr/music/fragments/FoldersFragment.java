package com.rr.music.fragments;

import android.content.Context;
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
    private ArrayList<HashMap<String, String>> hashMapList = new ArrayList<>();
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

                if (null != hashMapList)
                    hashMapList.clear();

                hashMapList = new MyMusicDB(mContext).getFolderNamesWithMusicImage();
                Log.d(LOG_TAG, "hashMapList.size(): " + hashMapList.size());

                mMusicAdapter = new MusicAdapter(hashMapList, Utilities.FOLDERS);

                mRecyclerView.setItemAnimator(new FadeInAnimator());
                AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mMusicAdapter);
                alphaAdapter.setFirstOnly(true);
                alphaAdapter.setDuration(1500);
                alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
                mRecyclerView.setAdapter(alphaAdapter);

                mMusicAdapter.setOnItemClickListener(new MusicAdapter.MyClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Log.d(LOG_TAG, "onItemClick(), position: " + position);
                    }

                    @Override
                    public void onItemLongClick(int position, View v) {
                        Log.d(LOG_TAG, "onItemLongClick(), position: " + position);
                    }
                });
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void updateRecyclerView () {
        if (null != hashMapList)
            hashMapList.clear();

        hashMapList = new MyMusicDB(mContext).getFolderNamesWithMusicImage();
        Log.d(LOG_TAG, "hashMapList.size(): " + hashMapList.size());

        mMusicAdapter.updateAdapter(hashMapList, Utilities.FOLDERS);
    }

}