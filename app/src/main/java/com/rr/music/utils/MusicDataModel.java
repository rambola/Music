package com.rr.music.utils;

public class MusicDataModel {
    private String songId;
    private String songAlbum;
    private String songArtist;
    private String songTitle;
    private String songData;
    private String songDisplayName;
    private String folderName;
    private long songDuration;

    public MusicDataModel (String songId, String songAlbum, String songArtist, String songTitle,
                           String songData, String songDisplayName,
                           String folderName, long songDuration) {
        this.songId = songId;
        this.songAlbum = songAlbum;
        this.songArtist = songArtist;
        this.songTitle = songTitle;
        this.songData = songData;
        this.songDisplayName = songDisplayName;
        this.folderName =folderName;
        this.songDuration = songDuration;
    }

    public String getSongId() {
        return songId;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongData() {
        return songData;
    }

    public String getSongDisplayName() {
        return songDisplayName;
    }

    public String getFolderName() {
        return folderName;
    }

    public long getSongDuration() {
        return songDuration;
    }

    public void setSongData(String songData) {
        this.songData = songData;
    }

    public void setSongDisplayName(String songDisplayName) {
        this.songDisplayName = songDisplayName;
    }
}