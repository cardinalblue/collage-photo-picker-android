//  Copyright Sep 2017-present Cardinalblue
//
//  Authors:
//  boy@cardinalblue.com,
//  yolung.lu@cardinalblue.com,
//  jack.huang@cardinalblue.com
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

package com.cardinalblue.photopicker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cardinalblue.photopicker.data.AlbumInfo;
import com.cardinalblue.photopicker.data.IAlbum;
import com.cardinalblue.photopicker.data.IPhoto;
import com.cardinalblue.photopicker.data.PhotoInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

public class GalleryPhotoLoader implements PhotoPickerContract.IPhotosLoader {

    private static final Uri MEDIA_STORE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    // Query for albums.
    private static final String[] PROJECTION_ALBUMS = {
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        String.format("COUNT(%s) AS %s", MediaStore.Images.Media.BUCKET_ID, BaseColumns._COUNT)
    };
    private static final String SELECTION_GROUP_BY =
        String.format("NOT TRIM(%s) = '') GROUP BY (2", MediaStore.Images.Media.DATA);
    private static final String SORT_ORDER_ALBUMS =
        MediaStore.Images.Media.DEFAULT_SORT_ORDER + ", "
        + MediaStore.Images.Media.DATE_ADDED;

    // Query for photos.
    private static final String[] PROJECTION_PHOTOS_IN_BUCKET = {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.ORIENTATION,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT
    };
    private static final String SELECTION_PHOTOS_IN_BUCKET =
        String.format("NOT TRIM(%s) = '' AND %s = ? ", MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID);
    private static final String SORT_ORDER_PHOTOS_IN_BUCKET =
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " DESC";

    // Given...
    private final ContentResolver mResolver;

    public GalleryPhotoLoader(ContentResolver resolver) {
        mResolver = resolver;
    }

    @Override
    public Observable<List<IAlbum>> loadAlbums() {
        return Observable
            .fromCallable(new Callable<List<IAlbum>>() {
                @Override
                public List<IAlbum> call() throws Exception {
                    final List<IAlbum> albums = new ArrayList<>();

                    Cursor cursor = null;
                    try {
                        cursor = mResolver.query(MEDIA_STORE_URI,
                                                 PROJECTION_ALBUMS,
                                                 SELECTION_GROUP_BY,
                                                 null,
                                                 SORT_ORDER_ALBUMS);
                        if (cursor == null || !cursor.moveToFirst()) {
                            return albums;
                        }

                        int thumbnailPathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                        int albumNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        int countColumn = cursor.getColumnIndexOrThrow(BaseColumns._COUNT);

                        // Key of the map is albumId
                        String albumId;
                        String albumName;
                        String thumbnailPath;
                        int photosCount;

                        do {
                            albumId = cursor.getString(albumIdColumn);
                            albumName = cursor.getString(albumNameColumn);
                            thumbnailPath = cursor.getString(thumbnailPathColumn);
                            photosCount = cursor.getInt(countColumn);

                            // If no data for Image, just skip this one
                            if (TextUtils.isEmpty(albumId) || TextUtils.isEmpty(albumName)
                                || TextUtils.isEmpty(thumbnailPath)) {
                                continue;
                            }

                            albums.add(new AlbumInfo(albumId, albumName, thumbnailPath, photosCount));
                        } while (cursor.moveToNext());
                    } catch (Throwable error) {
                        // IGNORED.
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    return albums;
                }
            });
    }

    @Override
    public Observable<Cursor> loadPhotosByAlbum(String albumId) {
        return new LoadPhotosInAlbumObservable(mResolver, albumId);
    }

    @Override
    public IAlbum toAlbum(Cursor cursor) {
        int thumbnailPathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
        int albumNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int countColumn = cursor.getColumnIndexOrThrow(BaseColumns._COUNT);

        String albumId = cursor.getString(albumIdColumn);
        String albumName = cursor.getString(albumNameColumn);
        String thumbnailPath = cursor.getString(thumbnailPathColumn);
        int photosCount = cursor.getInt(countColumn);

        // If no data for Image, just skip this one
        if (TextUtils.isEmpty(albumId) ||
            TextUtils.isEmpty(albumName) ||
            TextUtils.isEmpty(thumbnailPath)) {
            return null;
        } else {
            return new AlbumInfo(albumId, albumName, thumbnailPath, photosCount);
        }
    }

    @Override
    public IPhoto toPhoto(Cursor cursor, boolean isGetSize) {
        String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        if (url == null || url.length() == 0) {
            return new PhotoInfo("", "", 0, 0); // return a empty object
        }

        if (!isGetSize) {
            return new PhotoInfo(url, url, 0, 0); // FIXME
        } else {
            int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

            if (orientation == 90 || orientation == 270) {
                return new PhotoInfo(url, url, height, width);
            } else {
                return new PhotoInfo(url, url, width, height);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class LoadPhotosInAlbumObservable extends Observable<Cursor> {

        final ContentResolver resolver;
        final String albumId;

        LoadPhotosInAlbumObservable(ContentResolver resolver,
                                    String id) {
            this.resolver = resolver;
            this.albumId = id;
        }

        @Override
        protected void subscribeActual(Observer<? super Cursor> observer) {
            final String[] selectionArgs = new String[]{albumId};
            final Cursor cursor = resolver.query(MEDIA_STORE_URI,
                                                 PROJECTION_PHOTOS_IN_BUCKET,
                                                 SELECTION_PHOTOS_IN_BUCKET,
                                                 selectionArgs,
                                                 SORT_ORDER_PHOTOS_IN_BUCKET);

            observer.onSubscribe(new CursorDisposable(cursor));

            if (cursor == null) {
                // If error, still return valid cursor with zero item.
                observer.onNext(new MatrixCursor(new String[]{BaseColumns._ID,
                                                              BaseColumns._COUNT},
                                                 0));
            } else {
                observer.onNext(cursor);
            }
        }
    }

    private static class CursorDisposable extends MainThreadDisposable {

        // Given...
        final Cursor cursor;

        CursorDisposable(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        protected void onDispose() {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
