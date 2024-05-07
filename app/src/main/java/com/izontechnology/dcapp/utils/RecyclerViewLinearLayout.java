package com.izontechnology.dcapp.utils;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RecyclerViewLinearLayout {
    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL = LinearLayoutManager.VERTICAL;
    public static final int STAGGERED_VERTICAL = StaggeredGridLayoutManager.VERTICAL;
    public static final int STAGGERED_HORIZONTAL = StaggeredGridLayoutManager.HORIZONTAL;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {
    }
}
