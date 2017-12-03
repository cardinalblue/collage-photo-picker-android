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

package com.cardinalblue.photopicker.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * <p>
 * The grid item decoration for vertical orientation.
 * </p>
 * <p>
 * Note: The given {@link RecyclerView}'s padding would be changed!
 * </p>
 */
public class GridSpacingDecoration extends RecyclerView.ItemDecoration {

    private final int mGridSpacingPx;

    public GridSpacingDecoration(RecyclerView parent,
                                 int gridSpacingPx) {
        mGridSpacingPx = gridSpacingPx / 2;

        parent.setPadding(parent.getPaddingLeft() + mGridSpacingPx,
                          parent.getPaddingTop() + mGridSpacingPx,
                          parent.getPaddingRight() + mGridSpacingPx,
                          parent.getPaddingBottom() + mGridSpacingPx);
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        outRect.set(mGridSpacingPx, mGridSpacingPx,
                    mGridSpacingPx, mGridSpacingPx);
    }
}
