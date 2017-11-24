package com.rr.music.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.rr.music.R;
import com.rr.music.utils.MusicDataModel;

import java.util.ArrayList;
import java.util.List;

public class AlphabeticMusicAdapter extends RecyclerView.Adapter<AlphabeticMusicAdapter.DataObjectHolder>
        implements SectionIndexer {
    private static String LOG_TAG = AlphabeticMusicAdapter.class.getSimpleName();
    private List<MusicDataModel> mDataSet;
    private ArrayList<Integer> mSectionPositions;
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

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = mDataSet.size(); i < size; i++) {
            char section = mDataSet.get(i).getSongDisplayName().trim().charAt(0);
            if (!sections.contains(String.valueOf(section))) {
                if (Character.isDigit(section)) {
                    sections.add(String.valueOf(section));
                } else {
                    sections.add(String.valueOf(section).toUpperCase());
                }

                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int i) {
        return mSectionPositions.get(i);
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
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