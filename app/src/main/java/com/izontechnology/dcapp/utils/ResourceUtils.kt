package com.izontechnology.dcapp.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.izontechnology.dcapp.base.BaseApplication
import java.util.Collections


object ResourceUtils {

    fun getString(@StringRes stringId: Int): String {
        return BaseApplication.INSTANCE.getString(stringId)
    }

     fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    fun getDrawable(@DrawableRes drawableId: Int): Drawable? {
        return ContextCompat.getDrawable(BaseApplication.INSTANCE, drawableId)
    }

    fun getColor(@ColorRes colorId: Int): Int {
        return ContextCompat.getColor(BaseApplication.INSTANCE, colorId)
    }

    fun getDimen(@DimenRes dimenId: Int): Float {
        return BaseApplication.INSTANCE.getResources().getDimension(dimenId)
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getStringArrayList(@ArrayRes stringArrayID: Int): ArrayList<String> {
        val strings = ArrayList<String>()
        val stringArray: Array<String> =
            BaseApplication.INSTANCE.getResources().getStringArray(stringArrayID)
        Collections.addAll(strings, *stringArray)
        return strings
    }

    fun getIntArrayList(@ArrayRes stringArrayID: Int): ArrayList<Int> {
        val integers = ArrayList<Int>()
        val intArray: IntArray =
            BaseApplication.INSTANCE.getResources().getIntArray(stringArrayID)
        for (anIntArray in intArray) {
            integers.add(anIntArray)
        }
        return integers
    }

    fun getThemeName(context: Context, theme: Resources.Theme): String {
        return try {
            val mThemeResId: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val fThemeImpl = theme.javaClass.getDeclaredField("mThemeImpl")
                if (!fThemeImpl.isAccessible) fThemeImpl.isAccessible = true
                val mThemeImpl = fThemeImpl[theme]
                val fThemeResId = mThemeImpl.javaClass.getDeclaredField("mThemeResId")
                if (!fThemeResId.isAccessible) fThemeResId.isAccessible = true
                mThemeResId = fThemeResId.getInt(mThemeImpl)
            } else {
                val fThemeResId = theme.javaClass.getDeclaredField("mThemeResId")
                if (!fThemeResId.isAccessible) fThemeResId.isAccessible = true
                mThemeResId = fThemeResId.getInt(theme)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                theme.resources.getResourceEntryName(mThemeResId)
            } else context.resources.getResourceEntryName(mThemeResId)
        } catch (e: Exception) {
            // Theme returned by application#getTheme() is always Theme.DeviceDefault
            "AppTheme.ToolBar.White"
        }
    }

    fun getThemeName(@StyleRes theme: Int): String {
        return BaseApplication.INSTANCE.getResources().getResourceEntryName(theme)
    }

    fun String?.isWebUrl(): Boolean? {
        return android.util.Patterns.WEB_URL.matcher(this).matches()
    }
}