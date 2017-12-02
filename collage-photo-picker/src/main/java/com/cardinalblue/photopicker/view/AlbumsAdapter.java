package com.cardinalblue.photopicker.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.cardinalblue.photopicker.R;
import com.cardinalblue.photopicker.data.IAlbum;

import java.io.File;
import java.util.List;

public class AlbumsAdapter extends ArrayAdapter<IAlbum> {

    private final LayoutInflater mInflater;

    AlbumsAdapter(Context context, List<IAlbum> albumInfos) {
        super(context, R.layout.spinner_album_item, R.id.album_title, albumInfos);
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<IAlbum> albums) {
        clear();
        addAll(albums);
    }

    @NonNull
    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {
        return getCustomView(position, convertView, parent, R.layout.spinner_album_item);
    }

    @Override
    public View getDropDownView(int position,
                                View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent, R.layout.spinner_dropdown_album_item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private View getCustomView(int position,
                               View convertView,
                               ViewGroup parent,
                               int layoutId) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutId, parent, false);

            holder = new ViewHolder();
            holder.coverImageView = convertView.findViewById(R.id.album_cover);
            holder.titleTextView = convertView.findViewById(R.id.album_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final IAlbum album = getItem(position);
        if (album != null) {
            File thumbnailFile = new File(album.getThumbnailPath());
            Glide.with(getContext())
                 .load(thumbnailFile)
                 .apply(RequestOptions.placeholderOf(R.color.black_90))
                 .transition(DrawableTransitionOptions.withCrossFade())
                 .into(holder.coverImageView);

            holder.titleTextView.setText(album.getAlbumName());
        }

        return convertView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // View Holder /////////////////////////////////////////////////////////////////////////////////

    private static class ViewHolder {
        ImageView coverImageView;
        TextView titleTextView;
    }

}
