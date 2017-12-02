package com.cardinalblue.photopicker;

public interface IPhotoPickerLogger {

    void log(String... message);
    void logException(Throwable ex);
}
