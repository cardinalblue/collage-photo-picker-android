//  Copyright Sep 2017-present Cardinalblue
//
//  Author: jack.hunag@cardinalblue.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.cardinalblue.photopicker.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.TransitionFactory;
import com.cardinalblue.photopicker.R;
import com.cardinalblue.photopicker.data.IPhoto;
import com.cardinalblue.photopicker.PhotoPickerContract;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends CursorRecyclerViewAdapter<ViewHolder> {

    static final String PAYLOAD_SELECTED_PHOTO = "select_photo";
    static final String PAYLOAD_UNSELECTED_PHOTO = "un_select_photo";

    private static final int CAMERA_VIEW_TYPE = 0x00000001;
    private static final int IMAGE_VIEW_TYPE = 0x00000002;

    // Given.
    private final IOnTouchPhotoListener mListener;
    // Given optionally.
    private PhotoPickerContract.IPhotosLoader mPhotoLoader;

    // FIXME: should determine what value should be init ( or Injected form constructor)
    private boolean isEnableCamera = true;
    private boolean isEnableLongPress = false;

    PhotoAdapter(Context context,
                 IOnTouchPhotoListener listener) {
        super(context);

        mListener = listener;
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer CAMERA_VIEW_TYPE | SPAN_BANNER_VIEW_TYPE | IMAGE_VIEW_TYPE
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isEnableCamera) {
            return CAMERA_VIEW_TYPE;
        } else {
            return IMAGE_VIEW_TYPE;
        }
    }

    /**
     * Create new views (invoked by the layout manager).
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to.
     * @param viewType CAMERA_VIEW_TYPE | SPAN_BANNER_VIEW_TYPE | IMAGE_VIEW_TYPE
     * @return ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        switch (viewType) {
            case CAMERA_VIEW_TYPE:
                return new ViewHolder(getInflater().inflate(
                    R.layout.grid_item_camera, parent, false)) {};
            case IMAGE_VIEW_TYPE:
            default:
                return new ThumbnailViewHolder(getInflater().inflate(
                    R.layout.grid_item_photo, parent, false)) {};
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,
                                 int position,
                                 List<Object> payloads) {
        if (position == 0 && isEnableCamera) {
            // The camera call-to-action item.
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onTakePhoto();
                    }
                }
            });
        } else {
            // Photo item.
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder,
                                 final Cursor cursor,
                                 final List<Object> payloads) {
        final ThumbnailViewHolder thumbnailViewHolder = (ThumbnailViewHolder) viewHolder;
        // Normal photo thumbnail.
        final CheckableImageView imageView = thumbnailViewHolder.photoThumbnail;

        // With/without payload.
        if (payloads != null && !payloads.isEmpty()) {
            if (payloads.contains(PAYLOAD_SELECTED_PHOTO)) {
                imageView.setChecked(true);
            } else if (payloads.contains(PAYLOAD_UNSELECTED_PHOTO)) {
                imageView.setChecked(false);
            }
        } else {
            final IPhoto photo = mPhotoLoader.toPhoto(cursor, false);
            final File file = new File(photo.getSourceUrl());

            // FIXME: Adapter is responsible for this.
//            // Check/Un-check the view.
//            imageView.setChecked(mSelectionPool.getSelection().contains(photo));
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onSelectPhoto(viewHolder.getAdapterPosition(),
                                                photo, imageView.isChecked());
                    }
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null && isEnableLongPress) {
                        mListener.onLongPressPhoto(viewHolder.getAdapterPosition(),
                                                   photo, imageView.isChecked());
                    }
                    return true;
                }
            });

            // Load image.
            Glide.with(getContext())
                 .load(file)
                 .apply(RequestOptions.placeholderOf(R.color.black_90))
                 .transition(DrawableTransitionOptions.withCrossFade())
                 .into(imageView);
        }
    }

    @Override
    public void setData(Cursor cursor) throws Exception {
        super.setData(cursor);
        throw new IllegalAccessException(
            "Please use setData(cursor, loader) instead.");
    }

    public void setData(Cursor cursor,
                        PhotoPickerContract.IPhotosLoader loader) throws Exception{
        mPhotoLoader = loader;
        setData(cursor);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    void setEnableCamera(boolean enableCamera) {
        isEnableCamera = enableCamera;
    }

    void setEnableLongPress(boolean enableTouch) {
        isEnableLongPress = enableTouch;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        CheckableImageView photoThumbnail;

        ThumbnailViewHolder(View itemView) {
            super(itemView);

            photoThumbnail = (CheckableImageView) itemView;
        }
    }
}
