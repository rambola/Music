package com.rr.music.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rr.music.R;
import com.rr.music.utils.MusicDataModel;

import java.util.List;

public class FolderMusicListAdapter extends RecyclerView.Adapter<
        FolderMusicListAdapter.DataObjectHolder> {
    private static String LOG_TAG = MusicAdapter.class.getSimpleName();
    private List<MusicDataModel> mDataSet;
    private static MyClickListener myClickListener;

    public FolderMusicListAdapter(List<MusicDataModel> myDataSet) {
        mDataSet = myDataSet;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.music_adapter_layout, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.musicAdapterTV.setText(mDataSet.get(position).getSongDisplayName());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        TextView musicAdapterTV;

        DataObjectHolder(View itemView) {
            super(itemView);
            musicAdapterTV = itemView.findViewById(R.id.musicAdapterTV);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myClickListener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View view) {
            myClickListener.onItemLongClick(getAdapterPosition(), view);
            return true;
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        FolderMusicListAdapter.myClickListener = myClickListener;
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

}