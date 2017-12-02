package com.cardinalblue.photopicker.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumInfo implements IAlbum, Parcelable {

    private String albumId;
    private String albumName;
    private String thumbnailPath;
    private int photosCount;

    public AlbumInfo(String albumId,
                     String albumName,
                     String thumbnailPath,
                     int photosCount) {
        setAlbumId(albumId);
        setAlbumName(albumName);
        setThumbnailPath(thumbnailPath);
        setPhotosCount(photosCount);
    }

    @Override
    public String getAlbumId() {
        return albumId;
    }

    @Override
    public void setAlbumId(String newId) {
        albumId = newId;
    }

    @Override
    public String getAlbumName() {
        return albumName;
    }

    @Override
    public void setAlbumName(String newName) {
        albumName = newName;
    }

    @Override
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    @Override
    public void setThumbnailPath(String newPath) {
        thumbnailPath = newPath;
    }

    @Override
    public int getPhotosCount() {
        return photosCount;
    }

    @Override
    public void setPhotosCount(int newCount) {
        photosCount = newCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumId);
        dest.writeString(albumName);
        dest.writeString(thumbnailPath);
        dest.writeInt(photosCount);
    }

    public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {
        @Override
        public AlbumInfo createFromParcel(Parcel in) {
            return new AlbumInfo(in);
        }

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private AlbumInfo(Parcel in) {
        albumId = in.readString();
        albumName = in.readString();
        thumbnailPath = in.readString();
        photosCount = in.readInt();
    }
}
