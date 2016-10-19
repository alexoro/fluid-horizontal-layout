/*
 * Copyright (C) 2014 Alexander Sorokin (alexoro)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.alexoro.fluidhorizontallayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by uas.sorokin@gmail.com
 */
public class FluidHorizontalLayout extends ViewGroup {

    private int mGravity;


    public FluidHorizontalLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FluidHorizontalLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs, 0, 0);
    }

    public FluidHorizontalLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FluidHorizontalLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setWillNotDraw(true);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FHL_FluidHorizontalLayout);
            setGravity(a.getInt(R.styleable.FHL_FluidHorizontalLayout_gravity, Gravity.LEFT));
            a.recycle();
        } else {
            mGravity = Gravity.LEFT;
        }
    }


    // ============================================================

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FluidHorizontalLayout.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof FluidHorizontalLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }


    // ============================================================

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int countFluidView = countFluidViews();
        if (countFluidView != 1) {
            throw new IllegalStateException("This layout supports only one fluid layout. Found: " + countFluidView);
        }

        View fluidView = findFluidView();
        LayoutParams fluidLp = (LayoutParams) fluidView.getLayoutParams();

        int fixedWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
            if (!layoutParams.isFluid && v.getVisibility() != View.GONE) {
                measureChildWithMargins(
                        v,
                        widthMeasureSpec,
                        layoutParams.leftMargin + layoutParams.rightMargin,
                        heightMeasureSpec,
                        layoutParams.topMargin + layoutParams.bottomMargin);
                fixedWidth += v.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }
        }

        // measure fluid
        measureChildWithMargins(
                fluidView,
                widthMeasureSpec,
                fluidLp.leftMargin + fluidLp.rightMargin + fixedWidth,
                heightMeasureSpec,
                fluidLp.topMargin + fluidLp.bottomMargin);
        
        int rWidth = fixedWidth +
                fluidView.getMeasuredWidth() + fluidLp.leftMargin + fluidLp.rightMargin;

        int rHeight = fluidView.getMeasuredHeight() + fluidLp.topMargin + fluidLp.bottomMargin;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
            if (v != fluidView) {
                int itemHeight = v.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                if (itemHeight > rHeight) {
                    rHeight = itemHeight;
                }
            }
        }

        setMeasuredDimension(
                getMeasurement(
                        widthMeasureSpec,
                        rWidth + getPaddingLeft() + getPaddingRight()),
                getMeasurement(
                        heightMeasureSpec,
                        rHeight + getPaddingTop() + getPaddingBottom())
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mGravity == Gravity.RIGHT) {
            layoutFromRight(l, t, r, b);
        } else {
            layoutFromLeft(l, t, r, b);
        }
    }

    protected void layoutFromLeft(int l, int t, int r, int b) {
        int offsetX = l + getPaddingLeft();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            if (v.getVisibility() == View.GONE) {
                continue;
            }
            layoutVertical(v, lp, offsetX);
            offsetX += lp.leftMargin + lp.rightMargin + v.getMeasuredWidth();
        }
    }

    protected void layoutFromRight(int l, int t, int r, int b) {
        int offsetX = r - getPaddingRight();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            if (v.getVisibility() == View.GONE) {
                continue;
            }
            int left = offsetX - lp.rightMargin - v.getMeasuredWidth() - lp.leftMargin;
            layoutVertical(v, lp, left);
            offsetX = offsetX - lp.leftMargin - v.getMeasuredWidth() - lp.rightMargin;
        }
    }

    protected void layoutVertical(View v, LayoutParams lp, int left) {
        int height = getMeasuredHeight();
        if (lp.gravity == Gravity.BOTTOM) {
            v.layout(
                    left + lp.leftMargin,
                    height - getPaddingBottom() - lp.bottomMargin - v.getMeasuredHeight(),
                    left + lp.leftMargin + v.getMeasuredWidth(),
                    height - getPaddingBottom() - lp.bottomMargin);
        } else if (lp.gravity == Gravity.CENTER || lp.gravity == Gravity.CENTER_VERTICAL) {
            v.layout(
                    left + lp.leftMargin,
                    height / 2 - v.getMeasuredHeight() / 2,
                    left + lp.leftMargin + v.getMeasuredWidth(),
                    height / 2 + v.getMeasuredHeight() / 2);
        } else {
            v.layout(
                    left + lp.leftMargin,
                    getPaddingTop() + lp.topMargin,
                    left + lp.leftMargin + v.getMeasuredWidth(),
                    getPaddingTop() + lp.topMargin + v.getMeasuredHeight());
        }
    }

    protected int countFluidViews() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
            if (layoutParams.isFluid) {
                count++;
            }
        }
        return count;
    }

    protected View findFluidView() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
            if (layoutParams.isFluid) {
                return v;
            }
        }
        return null;
    }

    /**
     * Utility to return a view's standard measurement. Uses the
     * supplied size when constraints are given. Attempts to
     * hold to the desired size unless it conflicts with provided
     * constraints.
     *
     * @param measureSpec Constraints imposed by the parent
     * @param contentSize Desired size for the view
     * @return The size the view should be.
     */
    protected static int getMeasurement(int measureSpec, int contentSize) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case View.MeasureSpec.UNSPECIFIED:
                //Big as we want to be
                return contentSize;
            case View.MeasureSpec.AT_MOST:
                //Big as we want to be, up to the spec
                return Math.min(contentSize, specSize);
            case View.MeasureSpec.EXACTLY:
                //Must be the spec size
                return specSize;
            default:
                return 0;
        }
    }


    /**
     * Custom layout params
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public boolean isFluid;
        public int gravity;

        /**
         * Creates a new set of layout parameters.
         * @param c The application environment
         * @param attrs The set of attributes fom which to extract the layout  parameters values
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            if (attrs != null) {
                TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FHL_FluidHorizontalLayout);
                isFluid = a.getBoolean(R.styleable.FHL_FluidHorizontalLayout_layout_isFluid, false);
                gravity = a.getInt(R.styleable.FHL_FluidHorizontalLayout_layout_gravity, Gravity.NO_GRAVITY);
                a.recycle();
            } else {
                isFluid = false;
                gravity = Gravity.NO_GRAVITY;
            }
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}