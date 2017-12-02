//  Copyright Sep 2017-present CardinalBlue
//
//  Authors:
//  boy@cardinalblue.com,
//  yolung.lu@cardinalblue.com,
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

package com.cardinalblue.photopicker.model;

import com.cardinalblue.photopicker.data.IPhoto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class PhotoPickerStore {

    private final Object mMutex = new Object();

    private final List<IPhoto> mSelectionPool;
    private final Subject<PhotoPickerViewModel> mPickerStateSub = PublishSubject.create();

    public PhotoPickerStore(List<IPhoto> selectionPool) {
        mSelectionPool = selectionPool;
    }

    public boolean contains(IPhoto targetPhoto) {
        return getSelection().contains(targetPhoto);
    }

    public boolean select(int position,
                          IPhoto photo,
                          String albumId) {
        synchronized (mMutex) {
            // TODO: The inherent selection pool is an observable list for maximum
            // TODO: compatibility. e.g. Facebook photo picker.
            if (mSelectionPool.add(photo)) {
                mPickerStateSub.onNext(
                    PhotoPickerViewModel.select(
                        // Added.
                        Collections.singletonList(position),
                        Collections.singletonList(photo),
                        // Album Id.
                        albumId,
                        // Full list.
                        getSelection(),
                        // Focus position.
                        position));

                return true;
            } else {
                return false;
            }
        }
    }

    public void selectMultiple() {
        throw new RuntimeException("Yet supported.");
    }

    public boolean unSelect(int position,
                            IPhoto photo,
                            String albumId) {
        synchronized (mMutex) {
            // TODO: The inherent selection pool is an observable list for maximum
            // TODO: compatibility. e.g. Facebook photo picker.
            if (mSelectionPool.remove(photo)) {
                mPickerStateSub.onNext(
                    PhotoPickerViewModel.unSelect(
                        // Removed.
                        Collections.singletonList(position),
                        Collections.singletonList(photo),
                        // Album Id.
                        albumId,
                        // Full list.
                        getSelection(),
                        // Focus position.
                        position));
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean unSelectMultiple() {
        throw new RuntimeException("Yet supported.");
    }

    public void changeFocus(String albumId,
                            int position) {
        mPickerStateSub.onNext(
            PhotoPickerViewModel.updateFocus(
                // Album Id.
                albumId,
                // Full list.
                getSelection(),
                // Focus position.
                position));
    }

    public List<IPhoto> getSelection() {
        final List<IPhoto> clone = new ArrayList<>();

        synchronized (mMutex) {
            for (IPhoto photo : mSelectionPool) {
                clone.add(photo.copy());
            }
        }

        return clone;
    }

    public Observable<PhotoPickerViewModel> onSelectionUpdate() {
        return mPickerStateSub;
    }
}
