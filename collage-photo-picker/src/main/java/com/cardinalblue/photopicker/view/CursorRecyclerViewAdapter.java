/*
 * Copyright (C) 2014 skyfish.jy@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cardinalblue.photopicker.view;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {

    final private WeakReference<Context> mContext;
    final private WeakReference<LayoutInflater> mInflater;
    private Cursor mCursor;

    // State.
    private boolean mDataValid;
    private int mRowIdColumn;

    CursorRecyclerViewAdapter(Context context) {
        mContext = new WeakReference<>(context);
        mInflater = new WeakReference<>(LayoutInflater.from(context));
        mDataValid = false;
        mRowIdColumn = -1;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid &&
            mCursor != null && !mCursor.isClosed() &&
            mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        } else {
            return -1;
        }
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(VH viewHolder,
                                 int position) {
        this.onBindViewHolder(viewHolder, position, null);
    }

    @Override
    public void onBindViewHolder(VH viewHolder,
                                 int position,
                                 List<Object> payloads) {
        checkDataAndCursorOrThrow(position);
        onBindViewHolder(viewHolder, mCursor, payloads);
    }

    public abstract void onBindViewHolder(VH viewHolder,
                                          Cursor cursor,
                                          List<Object> payloads);

    @SuppressWarnings("unused")
    final public Context getContext() {
        return mContext.get();
    }

    @SuppressWarnings("unused")
    final public LayoutInflater getInflater() {
        return mInflater.get();
    }

    @SuppressWarnings("unused")
    final public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing
     * cursor it will be closed.
     */
    @SuppressWarnings("unused")
    public void setData(Cursor cursor) throws Exception {
        Cursor old = swapCursor(cursor);
        if (old != null && !old.isClosed()) {
            old.close();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected void checkDataAndCursorOrThrow(int position) {
        if (!mDataValid) {
            throw new IllegalStateException(
                "this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException(
                "couldn't move cursor to position " + position);
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor. Unlike
     * {@link #setData(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
            oldCursor.close();
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow(BaseColumns._ID);
            mDataValid = true;
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        notifyDataSetChanged();
        return oldCursor;
    }

    final private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    };
}
