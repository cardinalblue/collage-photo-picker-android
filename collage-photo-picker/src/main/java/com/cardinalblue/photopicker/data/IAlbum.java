package com.cardinalblue.photopicker.data;

import android.os.Parcelable;

public interface IAlbum extends Parcelable {

    String getAlbumId();

    String getAlbumName();

    String getThumbnailPath();

    int getPhotosCount();

    void setAlbumId(String id);

    void setAlbumName(String name);

    void setThumbnailPath(String path);

    void setPhotosCount(int count);
}
