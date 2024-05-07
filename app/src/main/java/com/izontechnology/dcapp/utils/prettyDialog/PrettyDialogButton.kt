package com.app.lifetidy.utils.prettyDialog

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.izontechnology.dcapp.R

class PrettyDialogButton : LinearLayout {
    internal var context: Context
    var text: String
    var text_color: Int?
    var background_color: Int?
    var tf: Typeface?
    var callback: () -> Unit
    lateinit var tv: TextView
    var default_background_color: Int
    var default_text_color: Int
    var iv: ImageView? = null

    constructor(context: Context, text: String, text_color: Int?, background_color: Int?, tf: Typeface?, callback: () -> Unit) : super(context) {
        this.context = context
        this.text = text
        this.text_color = text_color
        this.background_color = background_color
        this.tf = tf
        this.callback = callback
        this.default_background_color = R.color.pdlg_color_blue
        this.default_text_color = R.color.pdlg_color_white
        init()
    }

    private fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater?.inflate(R.layout.dialog_pretty_button, this)
        tv = findViewById(R.id.tv_button)
        tv.text = text
        context.resources?.let {
            tv.setTextColor(it.getColor((if (text_color == null) default_text_color else text_color)!!))
        }
        if (tf != null)
            tv.typeface = tf
        setBackground()
        setOnClickListener { v ->
            if (callback != null) {
                v.postDelayed({ callback.invoke() }, 150)
            }
        }
    }

    fun setTypeface(tf: Typeface) {
        this.tf = tf
        tv.typeface = tf
    }

    private fun setBackground() {
        setBackgroundDrawable(makeSelector(resources.getColor((if (background_color == null) default_background_color else background_color)!!)))
    }

    private fun getLightenColor(color: Int): Int {
        val fraction = 0.2
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)
        red = Math.min(red + red * fraction, 255.0).toInt()
        green = Math.min(green + green * fraction, 255.0).toInt()
        blue = Math.min(blue + blue * fraction, 255.0).toInt()
        val alpha = Color.alpha(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun makeSelector(color: Int): StateListDrawable {
        val res = StateListDrawable()
        res.setExitFadeDuration(150)
        val pressed_drawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(getLightenColor(color), getLightenColor(color)))
        pressed_drawable.cornerRadius = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
        val default_drawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, color))
        default_drawable.cornerRadius = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
        res.addState(intArrayOf(android.R.attr.state_pressed), pressed_drawable)
        res.addState(intArrayOf(), default_drawable)
        return res
    }
}