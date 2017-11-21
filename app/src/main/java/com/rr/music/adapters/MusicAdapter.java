package com.rr.music.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rr.music.R;
import com.rr.music.utils.GlideCircleTransform;
import com.rr.music.utils.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.DataObjectHolder> {
    private static String LOG_TAG = MusicAdapter.class.getSimpleName();
    private String mWhichFragment;
    private List<HashMap<String, String>> mHashMapList;
    private List<MusicDataModel> mDataSet;
    private static MyClickListener myClickListener;

    public MusicAdapter(List<MusicDataModel> myDataSet, String whichFragment) {
        mDataSet = myDataSet;
        mWhichFragment = whichFragment;
    }

    public MusicAdapter(ArrayList<HashMap<String, String>> myDataSet, String whichFragment) {
        mHashMapList = myDataSet;
        mWhichFragment = whichFragment;
    }

    public void updateAdapter(List<MusicDataModel> myDataSet, String whichFragment) {
        mDataSet = myDataSet;
        mWhichFragment = whichFragment;

        notifyDataSetChanged();
    }

    public void updateAdapter(ArrayList<HashMap<String, String>> myDataSet, String whichFragment) {
        mHashMapList = myDataSet;
        mWhichFragment = whichFragment;

        notifyDataSetChanged();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view;
        if (mWhichFragment.equalsIgnoreCase(Utilities.ALPHABETS)) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.music_adapter_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folders_music_adapter_layout, parent, false);
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        if (mWhichFragment.equalsIgnoreCase(Utilities.ALPHABETS)) {
            holder.musicAdapterTV.setText(mDataSet.get(position).getSongDisplayName());
        } else {
            Log.d(LOG_TAG, ""+mHashMapList.get(position).get(
                    Utilities.HASH_MAP_KEY_FOLDER_NAME).length());
            holder.folderNameTV.setText(mHashMapList.get(position).get(
                    Utilities.HASH_MAP_KEY_FOLDER_NAME));
            Glide.with(holder.musicImageIV.getContext()).load(mHashMapList.get(position).get(
                    Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH)).placeholder(R.mipmap.app_icon).
                    transform(new GlideCircleTransform(holder.musicImageIV.getContext())).
                    into(holder.musicImageIV);
        }
    }

    @Override
    public int getItemCount() {
        if (mWhichFragment.equalsIgnoreCase(Utilities.ALPHABETS)) {
            return mDataSet.size();
        } else {
            return mHashMapList.size();
        }
    }

    class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        TextView musicAdapterTV;
        TextView folderNameTV;
        ImageView musicImageIV;

        DataObjectHolder(View itemView) {
            super(itemView);
            if (mWhichFragment.equalsIgnoreCase(Utilities.ALPHABETS)) {
                musicAdapterTV = itemView.findViewById(R.id.musicAdapterTV);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            } else {
                folderNameTV = itemView.findViewById(R.id.folderNameTV);
                musicImageIV = itemView.findViewById(R.id.musicImageIV);

                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getPosition(), v);
        }

        @Override
        public boolean onLongClick(View view) {
            myClickListener.onItemLongClick(getPosition(), view);
            return true;
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        MusicAdapter.myClickListener = myClickListener;
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

}