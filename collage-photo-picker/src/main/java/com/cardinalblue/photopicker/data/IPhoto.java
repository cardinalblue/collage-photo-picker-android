package com.cardinalblue.photopicker.data;

import android.os.Parcelable;

public interface IPhoto extends Parcelable {

    int getWidth();
    int getHeight();
    void setWidth(float width);
    void setHeight(float height);

    /**
     * @return width over height.
     */
    double getAspectRatio();

    /**
     * The source URL.
     */
    String getSourceUrl();

    String getThumbnailUrl();

    boolean isIntrinsicallySlotable();

    IPhoto copy();
}
