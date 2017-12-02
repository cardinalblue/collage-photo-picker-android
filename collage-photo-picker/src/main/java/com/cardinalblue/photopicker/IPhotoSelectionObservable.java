package com.cardinalblue.photopicker;

import com.cardinalblue.photopicker.model.PhotoPickerViewModel;

import io.reactivex.Observable;

public interface IPhotoSelectionObservable {

    Observable<PhotoPickerViewModel> onSelectionUpdate();
}
