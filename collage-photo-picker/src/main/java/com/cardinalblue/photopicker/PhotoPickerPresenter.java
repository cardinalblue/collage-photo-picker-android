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

import android.Manifest;
import android.database.Cursor;

import com.cardinalblue.photopicker.data.IAlbum;
import com.cardinalblue.photopicker.data.IPhoto;
import com.cardinalblue.photopicker.model.CursorViewModel;
import com.cardinalblue.photopicker.model.PhotoPickerStore;
import com.cardinalblue.photopicker.model.PhotoPickerViewModel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class PhotoPickerPresenter
    implements IPresenter<PhotoPickerContract.IPhotoPickerView>,
               IPhotoSelectionObservable,
               IOnErrorObservable {

    // Given.
    private final RxPermissions mPermissionsHelper;
    private final PhotoPickerContract.IPhotosLoader mGalleryLoader;
    private final PhotoPickerStore mSelectionStore;
    private final Scheduler mUiScheduler;
    private final Scheduler mWorkScheduler;
    private final IPhotoPickerLogger mLogger;
    // Optional given.
    private PhotoPickerContract.IPhotoPickerView mPickerView;

    // Albums.
    private int mAlbumPosition = 0;
    private ArrayList<IAlbum> mAlbumList = new ArrayList<>();
    private final Subject<String> mRefreshPhotos = BehaviorSubject.<String>create().toSerialized();

    // Photos.
    private Cursor mPhotoCursor = null;
    private int mFocusPosition = PhotoPickerViewModel.IGNORED_POSITION;

    // Error.
    private final Subject<Throwable> mOnError = PublishSubject.<Throwable>create().toSerialized();

    // Disposable
    private final CompositeDisposable mDisposablesOnCreate = new CompositeDisposable();
    private final CompositeDisposable mDisposablesOnResume = new CompositeDisposable();

    public PhotoPickerPresenter(RxPermissions permissionsHelper,
                                PhotoPickerContract.IPhotosLoader galleryLoader,
                                PhotoPickerStore selectionStore,
                                Scheduler uiScheduler,
                                Scheduler workerScheduler,
                                IPhotoPickerLogger logger) {
        mPermissionsHelper = permissionsHelper;
        mGalleryLoader = galleryLoader;

        mSelectionStore = selectionStore;

        mUiScheduler = uiScheduler;
        mWorkScheduler = workerScheduler;

        mLogger = logger;
    }

    @Override
    public void bindViewOnCreate(PhotoPickerContract.IPhotoPickerView view) {
        mPickerView = view;

        // Setting from config.
        mPickerView.enableLongPress(false);
        mPickerView.enableCameraOption(true);

        // Ask for permissions and load albums.
        mDisposablesOnCreate.add(
            mPermissionsHelper
                // 1. Ask for permissions.
                .request(Manifest.permission.CAMERA,
                         Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // 2. Load albums if the permissions are granted.
                .flatMap(new Function<Boolean, ObservableSource<List<IAlbum>>>() {
                    @Override
                    public ObservableSource<List<IAlbum>> apply(Boolean granted) throws Exception {
                        if (granted) {
                            return mGalleryLoader
                                .loadAlbums()
                                .subscribeOn(mWorkScheduler);
                        } else {
                            return Observable.<List<IAlbum>>just(new ArrayList<IAlbum>());
                        }
                    }
                })
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<List<IAlbum>>() {
                    @Override
                    public void accept(@NonNull List<IAlbum> albums) throws Exception {
                        if (albums.isEmpty()) {
                            mAlbumPosition = -1;

                            mPickerView.showPermissionDeniedPrompt();
                        } else {
                            mAlbumPosition = 0;

                            mPickerView.setAlbums(albums, mAlbumPosition);
                            mAlbumList.addAll(albums);
                        }
                    }
                }));

        // Click on album.
        mDisposablesOnCreate.add(
            mPickerView
                .onClickAlbum()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String id) throws Exception {
                        for (int i = 0; i < mAlbumList.size(); ++i) {
                            final IAlbum album = mAlbumList.get(i);
                            if (album.getAlbumId().equals(id)) {
                                // Hold album position.
                                mAlbumPosition = i;
                                break;
                            }
                        }

                        mRefreshPhotos.onNext(id);
                    }
                }));

        // Load/refresh photos.
        mDisposablesOnCreate.add(
            mRefreshPhotos
                .switchMap(new Function<String, ObservableSource<CursorViewModel>>() {
                    @Override
                    public ObservableSource<CursorViewModel> apply(String id) throws Exception {
                        return mGalleryLoader
                            .loadPhotosByAlbum(id)
                            .subscribeOn(mWorkScheduler)
                            .map(new Function<Cursor, CursorViewModel>() {
                                @Override
                                public CursorViewModel apply(Cursor cursor) throws Exception {
                                    return CursorViewModel.succeed(cursor);
                                }
                            })
                            .startWith(CursorViewModel.inProgress(0))
                            .onErrorReturn(new Function<Throwable, CursorViewModel>() {
                                @Override
                                public CursorViewModel apply(Throwable error) throws Exception {
                                    return CursorViewModel.failed(error);
                                }
                            });
                    }
                })
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<CursorViewModel>() {
                    @Override
                    public void accept(CursorViewModel result) throws Exception {
                        try {
                            if (result.isSuccessful) {
                                final Cursor cursor = result.cursor;

                                // Hold cursor.
                                mPhotoCursor = cursor;

                                if (cursor.getCount() > 0) {
                                    mPickerView.setPhotosCursor(result.cursor,
                                                                mGalleryLoader,
                                                                // Immutable data.
                                                                getUrlSelectionSet());
                                    // Reset the scroll.
                                    mPickerView.scrollToPosition(0);
                                } else {
                                    mPickerView.showAlertForNotLoadingPhotos();
                                }
                            } else if (result.error != null) {
                                // Dispatch the error.
                                mOnError.onNext(result.error);

                                mPickerView.setPhotosCursor(null, null, null);
                                mPickerView.showAlertForNotLoadingPhotos();
                            }
                        } catch (Throwable any) {
                            mLogger.logException(any);
                        }
                    }
                }));

        // When User open camera.
        mDisposablesOnCreate.add(
            mPickerView
                .onClickCamera()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        mLogger.log("Adder menu - Use Camera");
                        mPickerView.openCamera();
                    }
                }));

        // When camera gives the photos.
        mDisposablesOnCreate.add(
            mPickerView
                .onTakePhotoFromCamera()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<IPhoto>() {
                    @Override
                    public void accept(IPhoto photo) throws Exception {
                        mLogger.log("Add Photos - Image from Camera");
                        mLogger.log("Add Photos", "from", "camera",
                                    "page", "photo picker",
                                    "num_of_image", String.valueOf(1));

                        // Actively update photos.
                        mRefreshPhotos.onNext(mAlbumList.get(mAlbumPosition)
                                                        .getAlbumId());
                    }
                }));

        // Click on thumbnail.
        mDisposablesOnCreate.add(
            mPickerView
                .onClickThumbnail()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer position) throws Exception {
                        if (mPhotoCursor == null ||
                            mPhotoCursor.isClosed() ||
                            position >= mPhotoCursor.getCount()) return;

                        mPhotoCursor.moveToFirst();
                        mPhotoCursor.move(position);

                        // TODO: Do it in the worker thread.
                        final IPhoto targetPhoto = mGalleryLoader.toPhoto(mPhotoCursor, true);
                        final boolean isSelected;
                        final String albumId = mAlbumList.get(mAlbumPosition).getAlbumId();

                        if (mSelectionStore.contains(targetPhoto)) {
                            // Un-selecting would notify the observers.
                            isSelected = !mSelectionStore.unSelect(position, targetPhoto, albumId);
                        } else {
                            // Selecting would notify the observers.
                            isSelected = mSelectionStore.select(position, targetPhoto, albumId);

                            if (isSelected) {
                                mLogger.log("Pick photo", "from", "library", "page", "photo picker");
                            } else {
                                // Cannot add photo due to exceed max number.
                                mPickerView.showAlertExceedMaxPhotoNumber();
                            }
                        }
                    }
                }));

        // Long-press on thumbnail.
        mDisposablesOnCreate.add(
            mPickerView
                .onLongPressThumbnail()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer position) throws Exception {
                        if (mPhotoCursor == null ||
                            mPhotoCursor.isClosed() ||
                            position >= mPhotoCursor.getCount()) return;

                        // For long press
                        mLogger.log("Photo picker - long press photo");
                        mLogger.log("Photo picker - preview", "action", "long press");

                        // Do whatever you want...
                    }
                }));

        // Click on preview icon.
        mDisposablesOnCreate.add(
            mPickerView
                .onClickPreviewIcon()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer position) throws Exception {
                        if (mPhotoCursor == null ||
                            mPhotoCursor.isClosed() ||
                            position >= mPhotoCursor.getCount()) return;

                        // For click preview icon
                        mLogger.log("Photo picker - preview", "action", "tap icon");

                        // Do whatever you want...
                    }
                }));

        // Photo selection.
        mDisposablesOnCreate.add(
            mSelectionStore
                .onSelectionUpdate()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<PhotoPickerViewModel>() {
                    @Override
                    public void accept(PhotoPickerViewModel state) throws Exception {
                        if (state.isUnSelectPhotos() || state.isSelectPhotos()) {
                            mPickerView.select(state);
                        }

                        // Hold focus position and use it later at bindIntentsOnResume().
                        mFocusPosition = state.focusPosition;
                    }
                }));
    }

    @Override
    public void unBindViewOnDestroy() {
        mDisposablesOnCreate.clear();

        // Recycle the albums.
        mAlbumList.clear();

        // Recycle the photo cursor.
        if (mPhotoCursor != null && !mPhotoCursor.isClosed()) {
            mPhotoCursor.close();
        }
    }

    @Override
    public void onResume() {
        // Scroll to focusPosition.
        if (mFocusPosition != PhotoPickerViewModel.IGNORED_POSITION) {
            mPickerView.scrollToPosition(mFocusPosition);
        }
    }

    @Override
    public void onPause() {
        mDisposablesOnResume.clear();
    }

    @Override
    public Observable<PhotoPickerViewModel> onSelectionUpdate() {
        return mSelectionStore.onSelectionUpdate();
    }

    @Override
    public Observable<Throwable> onError() {
        return mOnError;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private Set<String> getUrlSelectionSet() {
        final Set<String> set = new HashSet<>();

        for (IPhoto photo : mSelectionStore.getSelectionCopy()) {
            set.add(photo.getSourceUrl());
        }

        return set;
    }
}
