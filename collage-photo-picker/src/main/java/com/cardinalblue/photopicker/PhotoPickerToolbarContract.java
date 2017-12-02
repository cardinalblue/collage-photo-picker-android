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

import io.reactivex.Observable;

public final class PhotoPickerToolbarContract {

    public interface IToolbarView {

        void setEnabled(boolean enabled);

        void setToolbarTitle(String title);

        void setBackButtonType(int type);

        void enableDoneButton(boolean enabled, boolean visible);

        void enableSkipButton(boolean enabled, boolean visible);

        void setSelectionCount(int num);

        Observable<Object> onClickBackButton();

        Observable<Object> onClickDoneButton();

        Observable<Object> onClickSkipButton();
    }

    private PhotoPickerToolbarContract() {
        // Private constructor make this class like a namespace.
    }
}