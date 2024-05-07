package com.izontechnology.dcapp.utils

import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

fun View.showKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.showSoftInput(this, 0)
}

fun View.hideSoftKeyboard() {
    val imm = this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Activity.hideSoftKeyboard() {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = this.currentFocus
    if (view == null)
        view = View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toInvisible() {
    this.visibility = View.GONE
}

/**
 * Transforms static java function Snackbar.make() to an extension function on View.
 */
fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).run {
        addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                /*
                                EspressoIdlingResource.increment()
                */
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                /*
                                EspressoIdlingResource.decrement()
                */
            }
        })
        show()
    }
}

/**
 * Triggers a snackbar message when the value contained by snackbarTaskMessageLiveEvent is modified.
 */
fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner,
    snackbarEvent: LiveData<SingleEvent<Any>>,
    timeLength: Int
) {
    snackbarEvent.observe(
        lifecycleOwner,
        Observer { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is String -> {

                        showSnackbar(it, timeLength)
                    }

                    is Int -> {

                        showSnackbar(this.context.getString(it), timeLength)
                    }

                    else -> {
                    }
                }
            }
        }
    )
}

fun View.showToast(
    lifecycleOwner: LifecycleOwner,
    ToastEvent: LiveData<SingleEvent<Any>>,
    timeLength: Int
) {

    ToastEvent.observe(
        lifecycleOwner,
        Observer { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is String -> Toast.makeText(this.context, it, timeLength).show()
                    is Int -> Toast.makeText(this.context, this.context.getString(it), timeLength)
                        .show()

                    else -> {
                    }
                }
            }
        }
    )
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun AppCompatTextView.setTextFutureExt(text: String) =
    setTextFuture(
        PrecomputedTextCompat.getTextFuture(
            text,
            TextViewCompat.getTextMetricsParams(this),
            null
        )
    )

fun AppCompatEditText.setTextFutureExt(text: String) =
    setText(
        PrecomputedTextCompat.create(text, TextViewCompat.getTextMetricsParams(this))
    )

fun TextView.setGradientTextColor(vararg colorRes: Int) {
    this.post {
        val floatArray = ArrayList<Float>(colorRes.size)
        for (i in colorRes.indices) {
            floatArray.add(i, i.toFloat() / (colorRes.size - 1))
        }
        val textShader: Shader = LinearGradient(
            0f,
            0f,
            this.width.toFloat(),
            0f,
            colorRes.map { ContextCompat.getColor(this.context, it) }.toIntArray(),
            floatArray.toFloatArray(),
            Shader.TileMode.CLAMP
        )
        this.paint.shader = textShader
        this.invalidate()
    }
}

fun Long?.formatTime(): String {
    val hours = this?.div(3600)
    val minutes = (this?.rem(3600))?.div(60)
    val remainingSeconds = this?.rem(60)
    DeviceLogs.e("remaining sec","remaininig sec: $this")

    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}