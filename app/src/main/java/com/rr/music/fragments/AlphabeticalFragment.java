package com.rr.music.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rr.music.Dashboard;
import com.rr.music.R;
import com.rr.music.adapters.AlphabeticMusicAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.datamodels.MusicDataModel;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class AlphabeticalFragment extends Fragment implements SearchView.OnQueryTextListener {
    private final String LOG_TAG = AlphabeticalFragment.class.getSimpleName();
    private List<MusicDataModel> mMusicDataModels = new ArrayList<>();

    private Context mContext;
    private AlphabeticMusicAdapter mAlphabeticMusicAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(LOG_TAG, "isVisibleToUser: "+isVisibleToUser);

        if(isVisibleToUser && null != mAlphabeticMusicAdapter) {
            itemClickListener();
        }
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
            new MyMusicDB(mContext).clearTable();

            mRecyclerView = getView().findViewById(R.id.fragmentsRecyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mRecyclerView.setHasFixedSize(true);
            //        RecyclerView.ItemDecoration itemDecoration =
            //                new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
            //        mRecyclerView.addItemDecoration(itemDecoration);

            if (null != mMusicDataModels)
                mMusicDataModels.clear();

            mMusicDataModels = new MyMusicDB(mContext).getSongsAlphabeticalOrder();
            Log.d(LOG_TAG, "mMusicDataModels.size(): " + mMusicDataModels.size());

            mAlphabeticMusicAdapter = new AlphabeticMusicAdapter(mMusicDataModels);

            mRecyclerView.setItemAnimator(new FadeInAnimator());
            AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mAlphabeticMusicAdapter);
            alphaAdapter.setFirstOnly(true);
            alphaAdapter.setDuration(1500);
            alphaAdapter.setInterpolator(new OvershootInterpolator(2.5f));
            mRecyclerView.setAdapter(alphaAdapter);

            /*mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView,
                    new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, final int position) {
                    Log.d(LOG_TAG, "onItemClick(), position: " + position);
                    ((Dashboard) mContext).playMusic(mMusicDataModels, position);
                }

                @Override
                public void onLongClick(View view, int position) {
                    Log.d(LOG_TAG, "onItemLongClick(), position: " + position);
                }
            }));*/

            itemClickListener();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach " + context);
        mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_top:
                mRecyclerView.smoothScrollToPosition(0);
                return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<MusicDataModel> filteredModelList = filter(mMusicDataModels, newText);

        mAlphabeticMusicAdapter.setFilter(filteredModelList);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void showDeleteDialog(final int position, final String songId, final String songDisplayName,
                                  final String songPath) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(songDisplayName);
        alertDialog.setPositiveButton(mContext.getString(R.string.delete), null);
        alertDialog.setNegativeButton(mContext.getString(R.string.rename), null);
        alertDialog.setNeutralButton(mContext.getString(R.string.update), null);
        alertDialog.setMessage("You can Re-name or Delete the song.");

        final EditText input = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        input.setVisibility(View.GONE);

        final AlertDialog dialog = alertDialog.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isFileDeleted = deleteFileFromMediaStore(songId);

                        Log.d(LOG_TAG, "showDeleteDialog(), after deleting, " +
                                "isFileDeleted: " + isFileDeleted);
                        if (isFileDeleted) {
                            mMusicDataModels.remove(position);
                            mAlphabeticMusicAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.failedDelete),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        input.setVisibility(View.VISIBLE);
                        input.setText(songDisplayName);

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).
                                setVisibility(View.INVISIBLE);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).
                                setVisibility(View.VISIBLE);
                    }
                });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newName = input.getText().toString().trim();

                        String folderPath = songPath.substring(0, songPath.lastIndexOf("/"));
                        Log.d(LOG_TAG, "showDeleteDialog(), updating folderPath: " + folderPath);
                        Log.d(LOG_TAG, "showDeleteDialog(), songPath: " + songPath);

                        if (!TextUtils.isEmpty(newName)) {
                            if (!newName.contains(".mp3"))
                                newName = newName + ".mp3";

                            boolean renamed = renameFileInMediaStore(songId, newName);

                            Log.d(LOG_TAG, "showDeleteDialog(), renamed: " + renamed);

                            if (renamed) {
                                MusicDataModel musicDataModel = mMusicDataModels.get(position);
                                musicDataModel.setSongDisplayName(newName);
                                mAlphabeticMusicAdapter.notifyDataSetChanged();

                                dialog.dismiss();
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.failedRename),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(mContext, mContext.getString(R.string.nameEmtpy),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean deleteFileFromMediaStore(final String songId) {
        boolean deleted = false;

        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {songId};

        ContentResolver contentResolver = getActivity().getContentResolver();
        int del = contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                selection, selectionArgs);

        Log.d(LOG_TAG, "deleteFileFromMediaStore(), del: " + del);
        if (del > 0)
            deleted = true;

        return deleted;
    }

    private boolean renameFileInMediaStore(final String songId, final String newName) {
        boolean renamed = false;

        String selection = MediaStore.Audio.Media._ID + " = ? ";
        String[] selectionArgs = {songId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, newName);

        ContentResolver contentResolver = getActivity().getContentResolver();
        int update = contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues, selection, selectionArgs);
        Log.d(LOG_TAG, "renameFileInMediaStore(), update: " + update);
        if (update > 0)
            renamed = true;

        return renamed;
    }

    public void updateList() {
        mMusicDataModels = new MyMusicDB(mContext).getSongsAlphabeticalOrder();
        Log.d(LOG_TAG, "updateList(), mMusicDataModels.size(): " + mMusicDataModels.size());
        mAlphabeticMusicAdapter.updateAdapter(mMusicDataModels);
        itemClickListener();
    }

    private List<MusicDataModel> filter(List<MusicDataModel> models, String query) {
        query = query.toLowerCase();final List<MusicDataModel> filteredModelList = new ArrayList<>();
        for (MusicDataModel model : models) {
            final String text = model.getSongDisplayName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }

        return filteredModelList;
    }

    private void itemClickListener() {
        mAlphabeticMusicAdapter.setOnItemClickListener(new AlphabeticMusicAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, String songDisplayName, View view) {
                int itemPositionInFullList = indexInFullList(songDisplayName);
                Log.d(LOG_TAG, "onItemClick(), position: " + position +
                        ", songDisplayName: "+songDisplayName+", itemPositionInFullList: "+
                        itemPositionInFullList);
                if(-1 != itemPositionInFullList) {
                    ((Dashboard) mContext).playMusic(mMusicDataModels, itemPositionInFullList);
                }
            }

            @Override
            public void onItemLongClick(int position, String songDisplayName, View v) {
                int itemPositionInFullList = indexInFullList(songDisplayName);
                Log.d(LOG_TAG, "onItemLongClick(), position: " + position +
                        ", songDisplayName: "+songDisplayName+", itemPositionInFullList: "+
                        itemPositionInFullList);
                if(-1 != itemPositionInFullList) {
                    showDeleteDialog(itemPositionInFullList, mMusicDataModels.get(
                            itemPositionInFullList).getSongId(), mMusicDataModels.get(
                            itemPositionInFullList).getSongDisplayName(),
                            mMusicDataModels.get(itemPositionInFullList).getSongData());
                }
            }
        });
    }

    private int indexInFullList (String songDisplayName) {
        for(int i=0; i<mMusicDataModels.size(); i++) {
            if (mMusicDataModels.get(i).getSongDisplayName().equalsIgnoreCase(songDisplayName))
                return i;
        }
        return -1;
    }

}