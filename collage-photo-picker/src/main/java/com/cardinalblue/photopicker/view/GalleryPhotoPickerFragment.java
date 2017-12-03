//  Copyright Sep 2017-present CardinalBlue
//
//  Author: boy@cardinalblue.com
//          jack.hunag@cardinalblue.com,
//          yolung.lu@cardinalblue.com
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cardinalblue.photopicker.BuildConfig;
import com.cardinalblue.photopicker.IPhotoPickerPresenterProvider;
import com.cardinalblue.photopicker.IPresenter;
import com.cardinalblue.photopicker.PhotoPickerContract;
import com.cardinalblue.photopicker.R;
import com.cardinalblue.photopicker.data.IAlbum;
import com.cardinalblue.photopicker.data.IPhoto;
import com.cardinalblue.photopicker.data.PhotoInfo;
import com.cardinalblue.photopicker.model.PhotoPickerViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class GalleryPhotoPickerFragment
    extends Fragment
    implements PhotoPickerContract.IPhotoPickerView,
               IOnTouchPhotoListener {

    private static final Object IGNORED = new Object();

    // Views.
    protected Spinner mAlbumSpinner;
    protected RecyclerView mGalleryView;
    private PhotoAdapter mAdapter;

    // Albums.
    private static final String KEY_ALBUM_ID = "extra_album_id";
    private IAlbum mCurrentAlbumInfo;

    // Camera.
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String KEY_CAMERA_OUTPUT_URI = "key_camera_output_uri";
    private Uri mCameraOutputUri = null;

    // Presenter.
    private IPresenter<PhotoPickerContract.IPhotoPickerView> mPickerPresenter;

    // Subjects.
    private final Subject<IPhoto> mOnTakePhotoFromCamera = PublishSubject.create();
    private final Subject<Object> mOnClickCamera = PublishSubject.create();
    private final Subject<String> mOnClickAlbum = PublishSubject.create();
    private final Subject<Integer> mOnClickPhoto = PublishSubject.create();
    private final Subject<Integer> mOnLongClickPhoto = PublishSubject.create();
    private final Subject<Integer> mOnClickPreviewIcon = PublishSubject.create();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCameraOutputUri = savedInstanceState.getParcelable(KEY_CAMERA_OUTPUT_URI);
            mCurrentAlbumInfo = savedInstanceState.getParcelable(KEY_ALBUM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_photo_picker, container, false);

        // The dropdown menu for switching albums.
        mAlbumSpinner = mainView.findViewById(R.id.album_spinner);
        mAlbumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentAlbumInfo = (IAlbum) mAlbumSpinner.getAdapter().getItem(position);
                mOnClickAlbum.onNext(mCurrentAlbumInfo.getAlbumId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // DO NOTHING.
            }
        });
        mAlbumSpinner.setAdapter(new AlbumsAdapter(
            getActivity(), new ArrayList<IAlbum>()));

        mAdapter = new PhotoAdapter(getContext(), this);

        // The gallery view displaying the photos.
        mGalleryView = mainView.findViewById(R.id.photos_picker);
        mGalleryView.setHasFixedSize(true);
        mGalleryView.setAdapter(mAdapter);
        mGalleryView.setLayoutManager(
            new GridLayoutManager(getActivity(), 4));
        mGalleryView.addItemDecoration(
            new GridSpacingDecoration(
                mGalleryView, getResources().getDimensionPixelSize(
                R.dimen.grid_item_spacing_medium)));

        // Presenter.
        if (getActivity() instanceof IPhotoPickerPresenterProvider) {
            mPickerPresenter = ((IPhotoPickerPresenterProvider) getActivity()).getPhotoPickerPresenter();
            if (mPickerPresenter != null) {
                mPickerPresenter.bindViewOnCreate(this);
            }
        }

        return mainView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mPickerPresenter != null) {
            mPickerPresenter.unBindViewOnDestroy();
        }

        // Unregister the adapter.
        // Because the RecyclerView won't unregister the adapter, the
        // ViewHolders are very likely leaked.
        mGalleryView.setAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mPickerPresenter != null) {
            mPickerPresenter.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mPickerPresenter != null) {
            mPickerPresenter.onPause();
        }
    }

    @Override
    public void onTakePhoto() {
        mOnClickCamera.onNext(IGNORED);
    }

    @Override
    public void onSelectPhoto(int position) {
        mOnClickPhoto.onNext(position);
    }

    @Override
    public void onLongPressPhoto(int position) {
        mOnLongClickPhoto.onNext(position);
    }

    @Override
    public void onClickPreviewIcon(int position) {
        mOnClickPreviewIcon.onNext(position);
    }

    @Override
    public void onLongPressPreviewIcon(int position) {
        onClickPreviewIcon(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        findCameraPhotoPath(data)
                            .observeOn(Schedulers.io())
                            .map(new Function<Uri, IPhoto>() {
                                @Override
                                public IPhoto apply(Uri uri) throws Exception {
                                    // Decode the URI to get real file path.
                                    final String url = Uri.decode(uri.toString());

                                    // Get the size.
                                    final Rect size = getOriginalSize(getActivity(), uri);
                                    int width = 0;
                                    int height = 0;
                                    if (size != null) {
                                        width = size.width();
                                        height = size.height();
                                    }

                                    return new PhotoInfo(url, url, width, height);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<IPhoto>() {
                                @Override
                                public void accept(final IPhoto photo) throws Exception {
                                    // If the photo is saved, add to system media scanner.
                                    MediaScannerConnection.scanFile(
                                        getActivity(),
                                        new String[]{mCameraOutputUri.getPath()},
                                        null,
                                        new MediaScannerConnection.MediaScannerConnectionClient() {
                                            @Override
                                            public void onMediaScannerConnected() {

                                            }

                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {
                                                // Dispatch result.
                                                mOnTakePhotoFromCamera.onNext(photo);
                                            }
                                        });

                                    // Clear uri.
                                    mCameraOutputUri = null;
                                }
                            });
                    } catch (Throwable error) {
                        Toast.makeText(getActivity(),
                                       "Sorry, failed to insert the photo from Camera.",
                                       Toast.LENGTH_LONG)
                             .show();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putParcelable(KEY_CAMERA_OUTPUT_URI, mCameraOutputUri);
        saveState.putParcelable(KEY_ALBUM_ID, mCurrentAlbumInfo);
    }

    @Override
    public Observable<String> onClickAlbum() {
        return mOnClickAlbum;
    }

    @Override
    public Observable<Object> onClickCamera() {
        return mOnClickCamera;
    }

    @Override
    public Observable<IPhoto> onTakePhotoFromCamera() {
        return mOnTakePhotoFromCamera;
    }

    @Override
    public Observable<Integer> onLongPressThumbnail() {
        return mOnLongClickPhoto;
    }

    @Override
    public Observable<Integer> onClickThumbnail() {
        return mOnClickPhoto;
    }

    @Override
    public Observable<Integer> onClickPreviewIcon() {
        return mOnClickPreviewIcon;
    }

    @Override
    public void showProgressbar() {
        // DUMMY IMPL.
    }

    @Override
    public void hideProgressbar() {
        // DUMMY IMPL.
    }

    @Override
    public void showPermissionDeniedPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) return;

        // TODO: Show permission denied prompt.
        new AlertDialog.Builder(getActivity())
            .setMessage(getResources().getString(R.string.warning_permissions_denied))
            .create()
            .show();
//        mAdapter.setPermissionDenied();
    }

    @Override
    public void showAlertForNotLoadingPhotos() {
        // DUMMY IMPL.
    }

    @Override
    public void showAlertExceedMaxPhotoNumber() {
        // DUMMY IMPL.
    }

    @Override
    public void enableLongPress(boolean enabled) {
        mAdapter.setIsEnableLongPress(enabled);
    }

    @Override
    public void enableCameraOption(boolean enabled) {
        mAdapter.setIsEnableCamera(enabled);
    }

    @Override
    public void setAlbums(List<IAlbum> albums,
                          int selectPosition) {
        if (albums != null && albums.size() > 0) {
            mAlbumSpinner.setEnabled(true);
            mAlbumSpinner.setSelection(selectPosition);

            AlbumsAdapter albumAdapter = (AlbumsAdapter) mAlbumSpinner
                .getAdapter();

            if (albumAdapter != null) {
                albumAdapter.setData(new ArrayList<>(albums));

                // Load photos of the album.
                int index = mAlbumSpinner.getSelectedItemPosition();
                mOnClickAlbum.onNext(albums.get(index).getAlbumId());
            }
        } else {
            mAlbumSpinner.setEnabled(false);

            // Show permission denied prompt.
            showPermissionDeniedPrompt();
        }
    }

    @Override
    public void setPhotosCursor(Cursor cursor,
                                PhotoPickerContract.IPhotosLoader loader,
                                Set<String> selection) throws Exception {
        mAdapter.setData(cursor, loader, selection);
    }

    @Override
    public void select(PhotoPickerViewModel state) {
        // Selected.
        for (int i = 0; i < state.addedPositions.size(); ++i) {
            final IPhoto added = state.addedPhotos.get(i);
            // TODO: Maybe it shouldn't depend on size/aspect-ratio of photo.
            if (added.getWidth() <= 0 || added.getHeight() <= 0) {
                continue;
            }

            int position = state.addedPositions.get(i);

            // Add 1 is because the position from the state represents the
            // position in the photos world, and here it displays the camera
            // button before the photos.
            mAdapter.notifyItemChanged(position + 1,
                                       PhotoAdapter.PAYLOAD_SELECTED_PHOTO);
        }

        // Unselected.
        for (int i = 0; i < state.removedPositions.size(); ++i) {
            final IPhoto removed = state.removedPhotos.get(i);
            // TODO: Maybe it shouldn't depend on size/aspect-ratio of photo.
            if (removed.getWidth() <= 0 || removed.getHeight() <= 0) {
                continue;
            }

            int position = state.removedPositions.get(i);

            // Add 1 is because the position from the state represents the
            // position in the photos world, and here it displays the camera
            // button before the photos.
            mAdapter.notifyItemChanged(position + 1,
                                       PhotoAdapter.PAYLOAD_UNSELECTED_PHOTO);
        }
    }

    @Override
    public void scrollToPosition(int position) {
        mGalleryView.smoothScrollToPosition(position);
    }

    @Override
    public void openCamera() {
        try {
            final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.ENGLISH);
            final File imageFile = new File(dir, "Photo " + formatter.format(new Date()) + "." + "jpg");

            // Hold the saved path.
            mCameraOutputUri = Uri.fromFile(imageFile);

            final String authority = BuildConfig.APPLICATION_ID + ".file_provider";
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri outputUri = FileProvider.getUriForFile(getActivity(),
                                                       authority,
                                                       imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // Android 4.x device will got SecurityException when accessing the file that provide by FileProvider
            // ref : https://medium.com/@a1cooke/using-v4-support-library-fileprovider-and-camera-intent-a45f76879d61#.wnrl5hj3c
            List<ResolveInfo> resolvedIntentActivities = getContext()
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, outputUri,
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (Throwable error) {
            Toast.makeText(getContext(),
                           error.getMessage(),
                           Toast.LENGTH_LONG)
                 .show();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private Observable<Uri> findCameraPhotoPath(final Intent data) {
        if (mCameraOutputUri != null) {
            return Observable.just(mCameraOutputUri);
        } else if (data != null) {
            final Uri fileUri = data.getData();
            if (fileUri != null) {
                return Observable.just(fileUri);
            } else if (data.hasExtra("data")) {
                return Observable
                    .fromCallable(new Callable<Uri>() {
                        @Override
                        public Uri call() throws Exception {
                            // Some devices or some camera app does not setData() based on
                            // our given file uri. Instead, they put the bitmap data directly
                            // within the intent. Thus, we have this case to fix this.
                            //
                            // One known device has this situation is *hTC Flyer*.
                            //
                            // Some intents, like Cameras, return the Bitmap directly via the
                            // 'data' extra. This needs to be done after the URI check as
                            // some galleries also seem to return a thumbnail this way, and
                            // we don't want the thumbnail.
                            final Bitmap bmp = data.getParcelableExtra("data");
                            if (bmp == null) {
                                throw new RuntimeException(
                                    "Can't get camera data from data.getParcelableExtra('data')");
                            }

                            return Uri.fromFile(savePicture(bmp));
                        }
                    })
                    .subscribeOn(Schedulers.io());
            }
        }

        throw new RuntimeException("Can't find any camera photo information from the intent");
    }

    private File savePicture(Bitmap bmp) throws IOException {
        // FIXME: Probably won't find mCameraOutputUri.
        final String savedPath = Uri.decode(mCameraOutputUri.toString());
        final File savedFile = new File(savedPath);

        final Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        final FileOutputStream fos = new FileOutputStream(savedPath);
        bmp.compress(format, 90, fos);
        fos.close();

        return savedFile;
    }

    private Rect getOriginalSize(Context context, Uri uri) {
        try {
            // Option to load size only.
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            // Get the width and height.
            switch (uri.getScheme().toLowerCase()) {
                case "assets": {
                    final String path = uri.getAuthority() + uri.getPath();
                    final InputStream inputStream = context.getAssets().open(path);
                    BitmapFactory.decodeStream(inputStream, null, option);
                    inputStream.close();
                    break;
                }
                case "content":
                case "file":
                default: {
                    BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),
                                               null, option);
                    break;
                }
            }

            int exifOrientation = getExifOrientation(uri.getPath());
            if (exifOrientation == 90 || exifOrientation == 270) {
                return new Rect(0, 0, option.outHeight, option.outWidth);
            } else {
                return new Rect(0, 0, option.outWidth, option.outHeight);
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Get orientation of the image from Exif information.
     * Possible values: 0, 90, 180, 270.
     * Returns 0 if can't get the information.
     */
    private int getExifOrientation(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }

        try {
            ExifInterface exif = new ExifInterface(filePath);
            int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return 0;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException exc) {
            return 0;
        }
    }
}
