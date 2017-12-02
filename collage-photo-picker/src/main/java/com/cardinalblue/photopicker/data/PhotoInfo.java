package com.cardinalblue.photopicker.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoInfo implements IPhoto, Parcelable {

    private final String mUrl;
    private final String mThumbnailUrl;
    private int mWidth;
    private int mHeight;

    public PhotoInfo(String url,
                     String thumbUrl,
                     int width,
                     int height) {
        mUrl = url;
        mThumbnailUrl = thumbUrl;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void setWidth(float width) {
        mWidth = (int) width;
    }
    @Override
    public void setHeight(float height) {
        mHeight = (int) height;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public double getAspectRatio() {
        if (mWidth > 0 &&  mHeight > 0) {
            return mWidth / mHeight;
        } else {
            return 0;
        }
    }

    @Override
    public String getSourceUrl() {
        return mUrl;
    }

    @Override
    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    @Override
    public boolean isIntrinsicallySlotable() {
        return true;
    }

    @Override
    public IPhoto copy() {
        return new PhotoInfo(getSourceUrl(),
                             getThumbnailUrl(),
                             getWidth(),
                             getHeight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoInfo photoInfo = (PhotoInfo) o;
        return mUrl.equals(photoInfo.mUrl);

    }

    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUrl);
        dest.writeString(this.mThumbnailUrl);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
    }

    public static final Creator<PhotoInfo> CREATOR = new Creator<PhotoInfo>() {
        @Override
        public PhotoInfo createFromParcel(Parcel source) {
            return new PhotoInfo(source);
        }

        @Override
        public PhotoInfo[] newArray(int size) {
            return new PhotoInfo[size];
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private PhotoInfo(Parcel in) {
        this.mUrl = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mWidth = in.readInt();
        this.mHeight = in.readInt();
    }
}
