package com.rr.music.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
        Log.d(LOG_TAG, "HASH_MAP_KEY_SONG_ALBUM_ART_PATH: " + mHashMapList.get(position).get(
                Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH)+", position: "+position);

        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mHashMapList.get(position).get(
                Utilities.HASH_MAP_KEY_SONG_DATA));
        InputStream inputStream = null;
        if (mmr.getEmbeddedPicture() != null) {
            inputStream = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(holder.folderMusicImageIV.getContext()).load(stream.toByteArray()).asBitmap().centerCrop().
                    transform(new GlideCircleTransform(holder.folderMusicImageIV.getContext())).
                    diskCacheStrategy(DiskCacheStrategy.ALL).
                    placeholder(R.mipmap.app_icon).into(holder.folderMusicImageIV);
        }
        mmr.release();*/

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

   /* private Bitmap getBitmapFromUri(Uri uri, Context context) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }*/

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