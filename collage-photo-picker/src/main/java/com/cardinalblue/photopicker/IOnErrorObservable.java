package com.cardinalblue.photopicker;

import io.reactivex.Observable;

public interface IOnErrorObservable {

    Observable<Throwable> onError();
}
