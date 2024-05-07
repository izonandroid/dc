package com.izontechnology.dcapp.utils.prettyDialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialog
import com.app.lifetidy.utils.prettyDialog.PrettyDialogButton
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.databinding.DialogPrettyBinding

class PrettyDialog(internal var context: Context) : AppCompatDialog(context) {

    var resources: Resources? = null
    var close_rotation_animation: RotateAnimation? = null
    var icon_animation = true
    var typeface: Typeface? = null
    var thisDialog: PrettyDialog
    lateinit var dialogPrettyBinding: DialogPrettyBinding

    init {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialogPrettyBinding = DialogPrettyBinding.inflate(LayoutInflater.from(context))
        setContentView(dialogPrettyBinding.root)
        setCancelable(true)
        resources = context.resources
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setFlags(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val displayMetrics = resources?.displayMetrics
        val pxWidth = displayMetrics?.widthPixels?.toFloat()
        if (isTablet(context)) {
            (pxWidth?.times(0.50))?.toInt()?.let { window?.setLayout(it, ViewGroup.LayoutParams.WRAP_CONTENT) }
        } else {
            (pxWidth?.times(0.75))?.toInt()?.let { window?.setLayout(it, ViewGroup.LayoutParams.WRAP_CONTENT) }
        }
        window?.attributes?.windowAnimations = R.style.pdlg_default_animation
        thisDialog = this
//        setupViews_Base()
    }

    private fun isTablet(context: Context): Boolean {
        val xlarge = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
        val large = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViews_Base() {
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        resources?.getDimensionPixelSize(R.dimen.pretty_dialog_icon)?.div(2)?.let { lp.setMargins(0, it, 0, 0) }
        dialogPrettyBinding.llContent?.layoutParams = lp
        resources?.let {
//            dialogPrettyBinding.llContent?.setPadding(0, (1.25 * it.getDimensionPixelSize(R.dimen.pretty_dialog_icon) / 2).toInt(), 0, 0)
        }

        close_rotation_animation = RotateAnimation(
            0f, 180f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        close_rotation_animation?.duration = 300
        close_rotation_animation?.repeatCount = Animation.ABSOLUTE
        close_rotation_animation?.interpolator = DecelerateInterpolator()
        close_rotation_animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                thisDialog.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        dialogPrettyBinding.ivIcon?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.7f
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1.0f
                    if (icon_animation) {
                        v.startAnimation(close_rotation_animation)
                    }
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
        dialogPrettyBinding.tvTitle?.visibility = View.GONE
        dialogPrettyBinding.tvMessage?.visibility = View.GONE
    }

    fun setGravity(gravity: Int): PrettyDialog {
        window!!.setGravity(gravity)
        return this
    }

    fun addButton(text: String, textColor: Int?, backgroundColor: Int?, /*BUTTON_TYPE type,*/ callback: () -> Unit): PrettyDialog {
        resources?.let {
            val button = PrettyDialogButton(context, text, textColor, backgroundColor, typeface, callback)
            val margin = it.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
            val marginTop = it.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.setMargins(0, 0, 0, 0)
            lp.weight = 1.0f
            button.layoutParams = lp
            if (dialogPrettyBinding.llButtons.childCount > 0) {
                var view = View(context)
                val lp = LinearLayout.LayoutParams(it.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp), ViewGroup.LayoutParams.MATCH_PARENT)
                view.layoutParams = lp
                resources?.getColor(R.color.color_C4C4C4)
                    ?.let { it1 -> view.setBackgroundColor(it1) }
                dialogPrettyBinding.llButtons?.addView(view)
            }

            dialogPrettyBinding.llButtons?.addView(button)
        }
        return this
    }

    fun setTitle(text: String?): PrettyDialog {
        if (text?.trim { it <= ' ' }?.isNotEmpty() == true) {
            dialogPrettyBinding.tvTitle?.visibility = View.VISIBLE
            dialogPrettyBinding.tvTitle?.text = text
        } else {
            dialogPrettyBinding.tvTitle?.visibility = View.GONE
        }
        return this
    }

    fun setTitleColor(color: Int?): PrettyDialog {
        // tv_title.setTextColor(ContextCompat.getColor(context,color==null?R.color.pdlg_color_black : color));
        resources?.getColor(color ?: R.color.pdlg_color_black)?.let { dialogPrettyBinding.tvTitle?.setTextColor(it) }
        return this
    }

    fun setMessage(text: String): PrettyDialog {
        if (text.trim { it <= ' ' }.isNotEmpty()) {
            dialogPrettyBinding.tvMessage.visibility = View.VISIBLE
            dialogPrettyBinding.tvMessage.text = text
        } else {
            dialogPrettyBinding.tvMessage.visibility = View.GONE
        }
        return this
    }

    fun setMessageColor(color: Int?): PrettyDialog {
        // tv_message.setTextColor(ContextCompat.getColor(context,color==null?R.color.pdlg_color_black :color));
        resources?.getColor(color ?: R.color.pdlg_color_black)?.let { dialogPrettyBinding.tvMessage?.setTextColor(it) }
        return this
    }

    fun setIcon(icon: Int?): PrettyDialog {
        dialogPrettyBinding.ivIcon.setImageResource(icon ?: R.mipmap.ic_launcher)
        icon_animation = false
        dialogPrettyBinding.ivIcon.setOnTouchListener(null)
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setIconCallback(callback: () -> Unit): PrettyDialog {
        dialogPrettyBinding.ivIcon.setOnTouchListener(null)
        dialogPrettyBinding.ivIcon.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.7f
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1.0f
                    callback.invoke()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setIcon(icon: Int?, callback: () -> Unit): PrettyDialog {
        icon_animation = false
        dialogPrettyBinding.ivIcon.setImageResource(icon ?: R.mipmap.ic_launcher)
        dialogPrettyBinding.ivIcon.setOnTouchListener(null)
        dialogPrettyBinding.ivIcon.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.7f
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1.0f
                    callback.invoke()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
        return this
    }

    fun setTypeface(tf: Typeface?): PrettyDialog {
        typeface = tf
        dialogPrettyBinding.tvTitle?.typeface = tf
        dialogPrettyBinding.tvMessage?.typeface = tf

        for (i in 0 until dialogPrettyBinding.llButtons.childCount) {
            val button = dialogPrettyBinding.llButtons.getChildAt(i) as PrettyDialogButton
            tf?.let { button.setTypeface(it) }
            button.requestLayout()
        }

        return this
    }

    fun setAnimationEnabled(enabled: Boolean): PrettyDialog {
        if (enabled) {
            window?.attributes?.windowAnimations = R.style.pdlg_default_animation
        } else {
            window?.attributes?.windowAnimations = R.style.pdlg_no_animation
        }
        return this
    }
}