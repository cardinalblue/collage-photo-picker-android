//  Copyright Sep 2017-present CardinalBlue
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

import com.cardinalblue.photopicker.model.PhotoPickerStore;
import com.cardinalblue.photopicker.model.PhotoPickerViewModel;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class PhotoPickerToolbarPresenter
    implements IPresenter<PhotoPickerToolbarContract.IToolbarView> {

    // Given.
//    private final PhotoPickerContract.INavigator mNavigator;
    private final PhotoPickerStore mSelectionStore;
    // Optional given.
    private PhotoPickerToolbarContract.IToolbarView mToolbarView;

    private final Scheduler mUiScheduler;
    private final Scheduler mWorkerScheduler;

    private final IPhotoPickerLogger mLogger;

    // Disposable
    private CompositeDisposable mDisposablesOnCreate;
    private CompositeDisposable mDisposablesOnResume;

    public PhotoPickerToolbarPresenter(PhotoPickerStore selectionStore,
                                       Scheduler uiScheduler,
                                       Scheduler workerScheduler,
                                       IPhotoPickerLogger logger) {
        mSelectionStore = selectionStore;

        mUiScheduler = uiScheduler;
        mWorkerScheduler = workerScheduler;

        mLogger = logger;
    }

    @Override
    public void bindViewOnCreate(PhotoPickerToolbarContract.IToolbarView view) {
        mToolbarView = view;

        // Update number of photo to selector button
        mToolbarView.setSelectionCount(mSelectionStore.getSelectionCopy().size());

        // Generate observable flows.
        mDisposablesOnCreate = new CompositeDisposable();

        // Done button.
        mDisposablesOnCreate.add(
            mToolbarView
                .onClickDoneButton()
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object ignored) throws Exception {
                        if (mSelectionStore.getSelectionCopy().isEmpty()) return;

                        mLogger.log("Add Photos - Image from Photo Library");
                        mLogger.log("Add Photos", "from", "library",
                                    "page", "photo picker",
                                    "num_of_image", String.valueOf(mSelectionStore.getSelectionCopy().size()));

//                        if (mNavigator != null) {
//                            ArrayList<IPhoto> results = new ArrayList<>(mSelectionStore.getSelectionCopy());
//                            mNavigator.navigateToNextPageWithResult(results);
//                        }
                    }
                }));

        // Back button.
        mDisposablesOnCreate.add(
            mToolbarView
                .onClickBackButton()
                .observeOn(mUiScheduler)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
//                        if (mNavigator != null) {
//                            mNavigator.navigateToPreviousPageWithoutResult();
//                        }
                    }
                }));

        // Photo selection
        mDisposablesOnCreate.add(
            mSelectionStore
                .onSelectionUpdate()
                .subscribe(new Consumer<PhotoPickerViewModel>() {
                    @Override
                    public void accept(PhotoPickerViewModel state) throws Exception {
                        mToolbarView.setSelectionCount(state.selection.size());
                    }
                }));
    }

    @Override
    public void unBindViewOnDestroy() {
        if (mDisposablesOnCreate != null) {
            mDisposablesOnCreate.clear();
            mDisposablesOnCreate = null;
        }
    }

    @Override
    public void onResume() {
        // Update number of photo to selector button
        mToolbarView.setSelectionCount(mSelectionStore.getSelectionCopy().size());

        mDisposablesOnResume = new CompositeDisposable();
    }

    @Override
    public void onPause() {
        if (mDisposablesOnResume != null) {
            mDisposablesOnResume.clear();
            mDisposablesOnResume = null;
        }
    }
}
