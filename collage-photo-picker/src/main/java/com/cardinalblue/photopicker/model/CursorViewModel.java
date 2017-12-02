// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.cardinalblue.photopicker.model;

import android.database.Cursor;

public final class CursorViewModel {

    public final boolean isSuccessful, isInProgress;
    public final Throwable error;
    public final Cursor cursor;
    public final int progress;

    public static CursorViewModel start() {
        return null;
    }

    public static  CursorViewModel inProgress(int progress) {
        return new CursorViewModel(true, false, null, progress, null);
    }

    public static  CursorViewModel succeed(Cursor cursor) {
        return new CursorViewModel(false, true, null, 100, cursor);
    }

    public static  CursorViewModel failed(Throwable error) {
        return new CursorViewModel(false, false, error, 0, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private CursorViewModel(boolean isInProgress,
                            boolean isSuccessful,
                            Throwable error,
                            int progress,
                            Cursor cursor) {
        this.isInProgress = isInProgress;
        this.isSuccessful = isSuccessful;
        this.error = error;

        this.progress = progress;
        this.cursor = cursor;
    }
}
