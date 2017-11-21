package com.rr.music.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rr.music.Dashboard;
import com.rr.music.R;
import com.rr.music.adapters.MusicAdapter;
import com.rr.music.database.MyMusicDB;
import com.rr.music.utils.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class AlphabeticalFragment extends Fragment {
    private final String LOG_TAG = AlphabeticalFragment.class.getSimpleName();
    private List<MusicDataModel> mMusicDataModels = new ArrayList<>();
    private Context mContext;
    private MusicAdapter mMusicAdapter;

    private RecyclerView mRecyclerView;

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

            mMusicAdapter = new MusicAdapter(mMusicDataModels, Utilities.ALPHABETS);

            mRecyclerView.setItemAnimator(new FadeInAnimator());
            AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mMusicAdapter);
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

            mMusicAdapter.setOnItemClickListener(new MusicAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.d(LOG_TAG, "onItemClick(), position: " + position);
                    ((Dashboard) mContext).playMusic(mMusicDataModels, position);
                }

                @Override
                public void onItemLongClick(int position, View v) {
                    Log.d(LOG_TAG, "onItemLongClick(), position: " + position);
                    showDeleteDialog(position, mMusicDataModels.get(position).getSongId(),
                            mMusicDataModels.get(position).getSongDisplayName(),
                            mMusicDataModels.get(position).getSongData());
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach " + context);
        mContext = context;
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
                            mMusicAdapter.notifyDataSetChanged();
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
                                mMusicAdapter.notifyDataSetChanged();

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
        mMusicAdapter.updateAdapter(mMusicDataModels, Utilities.ALPHABETS);
    }

}