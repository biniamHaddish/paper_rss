/*
 * Copyright (C) 2016 The Android Open Source Project
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


package com.biniisu.leanrss.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.ui.base.HomeActivity;

import java.util.LinkedHashMap;

/**
 * SectionedDividerDecoration is a {@link RecyclerView.ItemDecoration} that can be used as a divider
 * between items of a {@link LinearLayoutManager}. It supports both {@link #HORIZONTAL} and
 * {@link #VERTICAL} orientations.
 * <p>
 * <pre>
 *     mDividerItemDecoration = new SectionedDividerDecoration(recyclerView.getContext(),
 *             mLayoutManager.getOrientation());
 *     recyclerView.addItemDecoration(mDividerItemDecoration);
 * </pre>
 */
public class SectionedDividerDecoration extends RecyclerView.ItemDecoration {

    public static final String TAG = SectionedDividerDecoration.class.getSimpleName();

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private final Rect regularDividerBounds = new Rect();
    private final Rect preSectionDeviderBounds = new Rect();
    private Drawable regularDivderDrawable;
    private Drawable preDateSectionDrawable;
    private LinkedHashMap<Long, Integer> dateSectionIndex;
    private RecyclerView parent;
    private HomeActivity.FeedListAdapter feedListAdapter;
    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int mOrientation;

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link LinearLayoutManager}.
     *
     * @param context Current context, it will be used to access resources.
     */
    public SectionedDividerDecoration(Context context, RecyclerView parent) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        regularDivderDrawable = context.getDrawable(R.drawable.list_divider_drawable);
        preDateSectionDrawable = context.getDrawable(R.drawable.pre_date_sections_divider);
        a.recycle();
        setOrientation(VERTICAL);

        this.parent = parent;
        feedListAdapter = (HomeActivity.FeedListAdapter) parent.getAdapter();
        this.dateSectionIndex = dateSectionIndex;
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    /**
     * Sets the {@link Drawable} for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        regularDivderDrawable = drawable;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (parent.getLayoutManager() == null) {
            return;
        }


        if (mOrientation == VERTICAL) {
            drawVertical(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {

            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (feedListAdapter.isPreSection(position)) {
//                parent.getDecoratedBoundsWithMargins(child, preSectionDeviderBounds);
//                final int bottom = preSectionDeviderBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
//                final int top = bottom - preDateSectionDrawable.getIntrinsicHeight();
//                preDateSectionDrawable.setBounds(left, top, right, bottom);
//                preDateSectionDrawable.draw(canvas);
            } else {
                parent.getDecoratedBoundsWithMargins(child, regularDividerBounds);
                final int bottom = regularDividerBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
                final int top = bottom - regularDivderDrawable.getIntrinsicHeight();
                regularDivderDrawable.setBounds(left, top, right, bottom);
                regularDivderDrawable.draw(canvas);
            }


        }
        canvas.restore();
    }

    public void invalidateDecorations() {

        if (parent != null) {
            this.dateSectionIndex = dateSectionIndex;
            parent.invalidateItemDecorations();
        }
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
//            if (!feedListAdapter.isPreSection(parent.getChildAdapterPosition(view))) {
//                outRect.set(0, 0, 0, regularDivderDrawable.getIntrinsicHeight());
//            } else {
//                outRect.set(0, 0, 0, 0);
//            }
            outRect.set(0, 0, 0, regularDivderDrawable.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, regularDivderDrawable.getIntrinsicWidth(), 0);
        }
    }
}
