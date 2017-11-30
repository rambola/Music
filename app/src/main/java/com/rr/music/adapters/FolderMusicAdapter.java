package com.rr.music.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rr.music.R;
import com.rr.music.utils.GlideCircleTransform;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolderMusicAdapter extends RecyclerView.Adapter<FolderMusicAdapter.DataObjectHolder> {
    private final String LOG_TAG = FolderMusicAdapter.class.getSimpleName();
    private List<HashMap<String, String>> mHashMapList;
    private static MyClickListener myClickListener;

    public FolderMusicAdapter(ArrayList<HashMap<String, String>> myDataSet) {
        mHashMapList = myDataSet;
    }

    public void updateAdapter(ArrayList<HashMap<String, String>> myDataSet) {
        mHashMapList = myDataSet;

        notifyDataSetChanged();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folders_music_adapter_layout, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.folderNameTV.setText(mHashMapList.get(position).get(
                Utilities.HASH_MAP_KEY_FOLDER_NAME));
//        Log.d(LOG_TAG, "HASH_MAP_KEY_SONG_ALBUM_ART_PATH: " + mHashMapList.get(position).get(
//                Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH)+", position: "+position);

        Glide.with(holder.folderMusicImageIV.getContext()).load(Uri.parse(mHashMapList.get(position).get(
                Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH))).centerCrop().
                diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.app_icon).
                transform(new GlideCircleTransform(holder.folderMusicImageIV.getContext())).
                into(holder.folderMusicImageIV);
    }

    @Override
    public int getItemCount() {
        return mHashMapList.size();
    }

    class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView folderNameTV;
        ImageView folderMusicImageIV;

        DataObjectHolder(View itemView) {
            super(itemView);
            folderNameTV = itemView.findViewById(R.id.folderNameTV);
            folderMusicImageIV = itemView.findViewById(R.id.folderMusicImageIV);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myClickListener.onItemClick(getAdapterPosition(),
                    mHashMapList.get(getAdapterPosition()).get(
                            Utilities.HASH_MAP_KEY_FOLDER_NAME), view);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        FolderMusicAdapter.myClickListener = myClickListener;
    }

    public interface MyClickListener {
        void onItemClick(int position, String songDisplayName, View v);
    }

}