package com.rr.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rr.music.utils.MusicDataModel;
import com.rr.music.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyMusicDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyMusicDB";
    private static final int DATABASE_VERSION = 3;

    private final String TABLE_NAME = "MyMusicTable";
    private final String SONG_ID = "SongId";
    private final String SONG_ALBUM_ID = "AlbumId";
    private final String SONG_ALBUM = "Album";
    private final String SONG_ALBUM_ART_PATH = "AlbumArtPath";
    private final String SONG_ARTIST = "Artist";
    private final String SONG_DISPLAY_NAME = "DisplayName";
    private final String SONG_TITLE = "Title";
    private final String SONG_DURATION = "Duration";
    private final String SONG_DATA = "Path";
    private final String SONG_FOLDER_NAME = "FolderName";

    public MyMusicDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String COLUMN_ID = "_id";

        final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+"("+COLUMN_ID+
                " INTEGER PRIMARY KEY AUTOINCREMENT, "+SONG_ID+" TEXT NOT NULL UNIQUE, "+SONG_ALBUM_ID+" TEXT, "+
                SONG_ALBUM+" TEXT, "+SONG_ALBUM_ART_PATH+" TEXT, "+SONG_ARTIST+" TEXT, "+
                SONG_DISPLAY_NAME+" TEXT, "+ SONG_TITLE+" TEXT, "+ SONG_DURATION+" TEXT, "+
                SONG_DATA+" TEXT, "+ SONG_FOLDER_NAME+" TEXT);";

        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

        onCreate(sqLiteDatabase);
    }

    public void insertSongDetails(String songId, String albumId, String album, String albumArtPath,
                                  String artist, String displayName, String title, String duration,
                                  String path, String folderName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SONG_ID, songId);
        contentValues.put(SONG_ALBUM_ID, albumId);
        contentValues.put(SONG_ALBUM, album);
        contentValues.put(SONG_ALBUM_ART_PATH, albumArtPath);
        contentValues.put(SONG_ARTIST, artist);
        contentValues.put(SONG_DISPLAY_NAME, displayName);
        contentValues.put(SONG_TITLE, title);
        contentValues.put(SONG_DURATION, duration);
        contentValues.put(SONG_DATA, path);
        contentValues.put(SONG_FOLDER_NAME, folderName);

        sqLiteDatabase.insertWithOnConflict(TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);

        sqLiteDatabase.close();
    }

    public List<MusicDataModel> getSongsAlphabeticalOrder() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        List<MusicDataModel> list = new ArrayList<>();

        String[] columns = {SONG_ID, SONG_DISPLAY_NAME, SONG_DURATION, SONG_DATA};
        String orderBy = SONG_DISPLAY_NAME + " ASC";

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        if(null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                list.add(new MusicDataModel(cursor.getString(
                        cursor.getColumnIndex(SONG_ID)), "", "", "", cursor.getString(
                        cursor.getColumnIndex(SONG_DATA)), cursor.getString(
                        cursor.getColumnIndex(SONG_DISPLAY_NAME)), "", Long.parseLong(cursor.getString(
                        cursor.getColumnIndex(SONG_DURATION)))));
            }
        }

        if(null != cursor)
            cursor.close();
        sqLiteDatabase.close();

        return list;
    }

    public List<MusicDataModel> getSongsAlphabeticalOrderForFolder (String folderName) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        List<MusicDataModel> list = new ArrayList<>();

        String[] columns = {SONG_ID, SONG_DISPLAY_NAME, SONG_DURATION, SONG_DATA};
        String orderBy = SONG_DISPLAY_NAME + " ASC";
        String selection = SONG_FOLDER_NAME + "=?";
        String selectionArgs[] = {folderName};

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);

        if(null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                list.add(new MusicDataModel(cursor.getString(
                        cursor.getColumnIndex(SONG_ID)), "", "", "", cursor.getString(
                        cursor.getColumnIndex(SONG_DATA)), cursor.getString(
                        cursor.getColumnIndex(SONG_DISPLAY_NAME)), "", Long.parseLong(cursor.getString(
                        cursor.getColumnIndex(SONG_DURATION)))));
            }
        }

        if(null != cursor)
            cursor.close();
        sqLiteDatabase.close();

        return list;
    }

    public ArrayList<HashMap<String, String>> getFolderNamesWithMusicImage () {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        ArrayList<HashMap<String, String>> hashMapList = new ArrayList<>();

        String[] columns = {SONG_ID, SONG_FOLDER_NAME, SONG_DATA, SONG_ALBUM_ID, SONG_ALBUM_ART_PATH};
        String orderBy = SONG_FOLDER_NAME + " ASC";

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        if(null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String songId = cursor.getString(cursor.getColumnIndex(SONG_ID)).trim();
                String folderName = cursor.getString(cursor.getColumnIndex(SONG_FOLDER_NAME)).trim();
                String songData = cursor.getString(cursor.getColumnIndex(SONG_DATA)).trim();
                String songAlbumId = cursor.getString(cursor.getColumnIndex(SONG_ALBUM_ID)).trim();
                String songAlbumArtPath = cursor.getString(cursor.getColumnIndex(SONG_ALBUM_ART_PATH));

                if (checkForDuplicateFolderName(folderName, hashMapList)) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(Utilities.HASH_MAP_KEY_SONG_ID, songId);
                    hashMap.put(Utilities.HASH_MAP_KEY_FOLDER_NAME, folderName);
                    hashMap.put(Utilities.HASH_MAP_KEY_SONG_DATA, songData);
                    hashMap.put(Utilities.HASH_MAP_KEY_SONG_ALBUM_ID, songAlbumId);
                    hashMap.put(Utilities.HASH_MAP_KEY_SONG_ALBUM_ART_PATH, songAlbumArtPath);
                    hashMapList.add(hashMap);
                }
            }
        }

        if(null != cursor)
            cursor.close();
        sqLiteDatabase.close();

        return hashMapList;
    }

    private boolean checkForDuplicateFolderName (String folderName,
                                                 List<HashMap<String, String>> hashMapList) {
        if(hashMapList.size() >= 1) {

            for (int i=0; i<hashMapList.size(); i++) {
                if(folderName.equalsIgnoreCase(hashMapList.get(i).get(
                        Utilities.HASH_MAP_KEY_FOLDER_NAME)))
                    return false;
            }
            return true;
        } else {
            return true;
        }
    }

    public void clearTable () {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME, null, null);
        sqLiteDatabase.close();
    }

}