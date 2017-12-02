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

package com.cardinalblue.photopicker.model;

import com.cardinalblue.photopicker.data.IPhoto;

import java.util.Collections;
import java.util.List;

public class PhotoPickerViewModel {

    public static final int IGNORED_POSITION = -1;

    public final List<Integer> addedPositions;
    public final List<IPhoto> addedPhotos;

    public final List<Integer> removedPositions;
    public final List<IPhoto> removedPhotos;

    public final String albumId;
    public final int focusPosition;

    public final List<IPhoto> selection;

    public static PhotoPickerViewModel select(List<Integer> addedPositions,
                                              List<IPhoto> addedPhotos,
                                              String albumId,
                                              List<IPhoto> selection,
                                              int pickerFocusPosition) {
        return new PhotoPickerViewModel(addedPositions,
                                        addedPhotos,
                                        Collections.<Integer>emptyList(),
                                        Collections.<IPhoto>emptyList(),
                                        albumId,
                                        selection,
                                        pickerFocusPosition
        );
    }

    public static PhotoPickerViewModel unSelect(List<Integer> removedPositions,
                                                List<IPhoto> removedPhotos,
                                                String albumId,
                                                List<IPhoto> selection,
                                                int pickerFocusPosition) {
        return new PhotoPickerViewModel(Collections.<Integer>emptyList(),
                                        Collections.<IPhoto>emptyList(),
                                        removedPositions,
                                        removedPhotos,
                                        albumId,
                                        selection,
                                        pickerFocusPosition
        );
    }

    public static PhotoPickerViewModel updateFocus(String albumId,
                                                   List<IPhoto> selection,
                                                   int pickerFocusPosition) {
        return new PhotoPickerViewModel(Collections.<Integer>emptyList(),
                                        Collections.<IPhoto>emptyList(),
                                        Collections.<Integer>emptyList(),
                                        Collections.<IPhoto>emptyList(),
                                        albumId,
                                        selection,
                                        pickerFocusPosition
        );
    }

    public boolean isSelectPhotos() {
        return !addedPhotos.isEmpty();
    }

    public boolean isUnSelectPhotos() {
        return !removedPhotos.isEmpty();
    }

    public boolean isMovedFocusPosition() {
        return addedPhotos.isEmpty() &&
               removedPhotos.isEmpty() &&
               focusPosition != IGNORED_POSITION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoPickerViewModel that = (PhotoPickerViewModel) o;

        if (focusPosition != that.focusPosition) return false;
        if (addedPositions != null ? !addedPositions.equals(that.addedPositions) : that.addedPositions != null)
            return false;
        if (addedPhotos != null ? !addedPhotos.equals(that.addedPhotos) : that.addedPhotos != null) return false;
        if (removedPositions != null ? !removedPositions.equals(that.removedPositions) : that.removedPositions != null)
            return false;
        if (removedPhotos != null ? !removedPhotos.equals(that.removedPhotos) : that.removedPhotos != null)
            return false;
        if (albumId != null ? !albumId.equals(that.albumId) : that.albumId != null) return false;
        return selection != null ? selection.equals(that.selection) : that.selection == null;
    }

    @Override
    public int hashCode() {
        int result = addedPositions != null ? addedPositions.hashCode() : 0;
        result = 31 * result + (addedPhotos != null ? addedPhotos.hashCode() : 0);
        result = 31 * result + (removedPositions != null ? removedPositions.hashCode() : 0);
        result = 31 * result + (removedPhotos != null ? removedPhotos.hashCode() : 0);
        result = 31 * result + (albumId != null ? albumId.hashCode() : 0);
        result = 31 * result + focusPosition;
        result = 31 * result + (selection != null ? selection.hashCode() : 0);
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private PhotoPickerViewModel(List<Integer> addedPositions,
                                 List<IPhoto> addedPhotos,
                                 List<Integer> removedPositions,
                                 List<IPhoto> removedPhotos,
                                 String albumId,
                                 List<IPhoto> selection,
                                 int pickerFocusPosition) {
        this.addedPositions = addedPositions;
        this.addedPhotos = addedPhotos;
        this.removedPositions = removedPositions;
        this.removedPhotos = removedPhotos;
        this.albumId = albumId;
        this.selection = selection;
        this.focusPosition = pickerFocusPosition;
    }
}
