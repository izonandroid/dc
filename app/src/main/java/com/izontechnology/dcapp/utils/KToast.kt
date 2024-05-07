package com.izontechnology.dcapp.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    activity?.toast(message, duration)
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    activity?.toast(message, duration)
}