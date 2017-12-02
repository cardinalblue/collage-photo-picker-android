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

import android.database.Cursor;

import com.cardinalblue.photopicker.data.IAlbum;
import com.cardinalblue.photopicker.data.IPhoto;
import com.cardinalblue.photopicker.model.PhotoPickerViewModel;

import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

public final class PhotoPickerContract {

//    public interface IToolbarView {
//
//        void setEnabled(boolean enabled);
//
//        void setToolbarTitle(String title);
//
//        void setBackButtonType(int type);
//
//        void enableDoneButton(boolean enabled, boolean visible);
//
//        void enableSkipButton(boolean enabled, boolean visible);
//
//        void setSelectionCount(int num);
//
//        Observable<Object> onClickBackButton();
//
//        Observable<Object> onClickDoneButton();
//
//        Observable<Object> onClickSkipButton();
//    }

    public interface IPhotoPickerView {

        Observable<String> onClickAlbum();

        Observable<Object> onClickCamera();

        Observable<List<IPhoto>> onTakePhotoFromCamera();

        Observable<Integer> onLongPressThumbnail();

        Observable<Integer> onClickThumbnail();

        Observable<Integer> onClickPreviewIcon();

        void showProgressbar();

        void hideProgressbar();

        void showPrompt();

        void showAlertForNotLoadingPhotos();

        void showAlertExceedMaxPhotoNumber();

        void enableLongPress(boolean enabled);

        void enableCameraOption(boolean enabled);

        void setAlbums(List<IAlbum> albums, int selectPosition);

        void setPhotosCursor(Cursor cursor, IPhotosLoader loader, Set<String> selection) throws Exception;

        void select(PhotoPickerViewModel selectedPhotos);

        void scrollToPosition(int position);

        // TODO: Navigator's responsibility.
        void navigateToCameraView();
    }

    public interface IPhotosLoader {

        Observable<List<IAlbum>> loadAlbums();

        Observable<Cursor> loadPhotosByAlbum(String albumId);

        IAlbum toAlbum(Cursor c);

        IPhoto toPhoto(Cursor cursor, boolean isGetSize);
    }

    private PhotoPickerContract() {
        // Private constructor make this class like a namespace.
    }
}
