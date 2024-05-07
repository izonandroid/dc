package com.izontechnology.dcapp.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RecyclerViewLayoutManager {
    public static final int LINEAR = 10010;
    public static final int GRID = 10020;
    public static final int STAGGERED = 10030;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LINEAR, GRID, STAGGERED})
    public @interface LayoutManager {
    }
}
