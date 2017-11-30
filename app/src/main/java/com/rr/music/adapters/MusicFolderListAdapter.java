package com.rr.music.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rr.music.R;
import com.rr.music.datamodels.MusicDataModel;

import java.util.List;

public class MusicFolderListAdapter extends RecyclerView.Adapter<
        MusicFolderListAdapter.DataObjectHolder> {
    private static String LOG_TAG = AlphabeticMusicAdapter.class.getSimpleName();
    private int rowIndex = -1;
    private List<MusicDataModel> mDataSet;

    private static MyClickListener myClickListener;

    public MusicFolderListAdapter(List<MusicDataModel> myDataSet) {
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

        if(rowIndex == position) {
            holder.cardView.setCardBackgroundColor(holder.cardView.getContext().
                    getResources().getColor(R.color.lightGray));
        } else {
            holder.cardView.setCardBackgroundColor(holder.cardView.getContext().
                    getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        TextView musicAdapterTV;
        CardView cardView;

        DataObjectHolder(View itemView) {
            super(itemView);
            musicAdapterTV = itemView.findViewById(R.id.musicAdapterTV);
            cardView = itemView.findViewById(R.id.musicAdapterCardView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            rowIndex = getAdapterPosition();
            notifyDataSetChanged();
            myClickListener.onItemClick(rowIndex, view);
        }

        @Override
        public boolean onLongClick(View view) {
            myClickListener.onItemLongClick(getAdapterPosition(), view);
            return true;
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        MusicFolderListAdapter.myClickListener = myClickListener;
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public void newRowIndex (int newRowIndex) {
        rowIndex = newRowIndex;
        notifyDataSetChanged();
    }

}