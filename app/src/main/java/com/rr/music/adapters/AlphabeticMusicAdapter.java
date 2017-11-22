package com.rr.music.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rr.music.R;
import com.rr.music.utils.MusicDataModel;

import java.util.ArrayList;
import java.util.List;

public class AlphabeticMusicAdapter extends RecyclerView.Adapter<AlphabeticMusicAdapter.DataObjectHolder> {
    private static String LOG_TAG = AlphabeticMusicAdapter.class.getSimpleName();
    private List<MusicDataModel> mDataSet;
    private static MyClickListener myClickListener;

    public AlphabeticMusicAdapter(List<MusicDataModel> myDataSet) {
        mDataSet = myDataSet;
    }

    public void updateAdapter(List<MusicDataModel> myDataSet) {
        mDataSet = myDataSet;

        notifyDataSetChanged();
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
        CardView cardView;
        TextView folderNameTV;
        ImageView musicImageIV;

        DataObjectHolder(View itemView) {
            super(itemView);
            musicAdapterTV = itemView.findViewById(R.id.musicAdapterTV);
            cardView = itemView.findViewById(R.id.musicAdapterCardView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myClickListener.onItemClick(getAdapterPosition(),
                    mDataSet.get(getAdapterPosition()).getSongDisplayName(), view);
        }

        @Override
        public boolean onLongClick(View view) {
            myClickListener.onItemLongClick(getAdapterPosition(),
                    mDataSet.get(getAdapterPosition()).getSongDisplayName(), view);
            return true;
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        AlphabeticMusicAdapter.myClickListener = myClickListener;
    }

    public interface MyClickListener {
        void onItemClick(int position, String songDisplayName, View v);
        void onItemLongClick(int position, String songDisplayName, View v);
    }

    public void setFilter(List<MusicDataModel> dataSet) {
        mDataSet = new ArrayList<>();
        mDataSet.addAll(dataSet);
        notifyDataSetChanged();
    }

}