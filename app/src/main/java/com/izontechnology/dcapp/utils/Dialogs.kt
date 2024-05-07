package com.izontechnology.dcapp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.databinding.LayoutDialogBinding
import com.izontechnology.dcapp.databinding.LayoutInputDialogBinding
import com.izontechnology.dcapp.utils.prettyDialog.PrettyDialog

fun Context.toastMessage(message: String?) {
    message?.let {
        toast(message)
    }
}


fun Context.simpleAlert(msg: String,onCancel:(()->Unit?)? = null,onDismiss:(()->Unit?)? = null):Dialog {
    val dialogBinding = LayoutDialogBinding.inflate(LayoutInflater.from(this))
    val mDialog = AppCompatDialog(this)
    mDialog.setContentView(dialogBinding.root)
    mDialog.setCanceledOnTouchOutside(true)
    mDialog.setCancelable(true)
    mDialog?.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    mDialog.setOnCancelListener { onCancel?.invoke() }
    mDialog.setOnDismissListener { onDismiss?.invoke() }
    mDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    msg?.let { dialogBinding.message.text = it }
    mDialog.show()
    return mDialog
}

fun Context.simpleAlertOverLay(msg: String) {
    val dialogBinding = LayoutDialogBinding.inflate(LayoutInflater.from(this))
    val mDialog = AppCompatDialog(this)
    mDialog.setContentView(dialogBinding.root)
    mDialog.setCanceledOnTouchOutside(true)
    mDialog.setCancelable(true)
    mDialog?.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    mDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mDialog.getWindow()?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    } else {
        mDialog.getWindow()?.setType(WindowManager.LayoutParams.TYPE_PHONE);
    }
    msg?.let { dialogBinding.message.text = it }
    mDialog.show()
}

fun Context.simpleInputDialog(
    msg: String, onOK: ((dialog: Dialog, input: String, view: View) -> Unit)? = null,
    onCancel: ((dialog: Dialog, input: String, view:View) -> Unit)? = null):Dialog {
    val dialogBinding = LayoutInputDialogBinding.inflate(LayoutInflater.from(this))
    val mDialog = AppCompatDialog(this,R.style.MyDialogTheme)
    mDialog.setContentView(dialogBinding.root)
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog?.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    mDialog?.window?.setBackgroundDrawable(ColorDrawable(getColor(R.color.theme_white)))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mDialog.getWindow()?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    } else {
        mDialog.getWindow()?.setType(WindowManager.LayoutParams.TYPE_PHONE);
    }

    msg?.let { dialogBinding.message.text = it }
    mDialog.show()
    dialogBinding.txtOk.setOnClickListener {
        onOK?.invoke(mDialog, dialogBinding.password.text.toString().trim(),dialogBinding.password)
    }
    dialogBinding.txtCancel.setOnClickListener {
        mDialog.dismiss()
        onCancel?.invoke(mDialog, dialogBinding.password.text.toString().trim(),dialogBinding.password)
    }
    return mDialog
}

fun Context.simpleAlert(msg: String, positiveButton: (() -> Unit)? = null) {
    simpleAlert(null, msg, positiveButton)
}

fun Context.simpleAlert(
    title: String?,
    msg: String?,
    positiveButton: (() -> Unit)? = null,
    icon: Int? = R.drawable.ic_information
) {
    simpleAlert(title, msg, getString(R.string.ok), positiveButton, icon)
}

fun Context.simpleAppAlert(
    msg: String?,
    btnTitle: String?,
    positiveButton: (() -> Unit)? = null,
    icon: Int? = R.mipmap.ic_launcher
) {
    simpleAlert(
        getString(R.string.app_name),
        msg,
        btnTitle ?: getString(R.string.ok),
        positiveButton,
        icon
    )
}

fun Context.simpleAlert(
    title: String? = null,
    msg: String?,
    btnTitle: String?,
    positiveButton: (() -> Unit)? = null,
    icon: Int? = R.drawable.ic_information
) {
    val mDialog = PrettyDialog(this)
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(icon)
    mDialog.setTitle(title ?: resources.getString(R.string.app_name))
    msg?.let { mDialog.setMessage(it) }
    mDialog.addButton(btnTitle ?: "", R.color.color_174b71, R.color.color_EDEDED) {
        positiveButton?.invoke()
        mDialog.dismiss()
    }.show()
}

fun Context.confirmationDialog(
    msg: String,
    btnPositiveClick: (() -> Unit)? = null,
    btnNegativeClick: (() -> Unit)? = null
): PrettyDialog {
    val mDialog = PrettyDialog(this)
//    mDialog.setTypeface(this.font(R.font.montserrat_regular))
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(R.drawable.ic_information)
        .setTitle(getString(R.string.app_name))
        .setMessage(msg)
        .addButton(getString(R.string.yes), R.color.color_174b71, R.color.color_EDEDED) {
            btnPositiveClick?.invoke()
            mDialog.dismiss()
        }
        .addButton(getString(R.string.cancel), R.color.color_174b71, R.color.color_EDEDED) {
            btnNegativeClick?.invoke()
            mDialog.dismiss()
        }
        .show()
    return mDialog
}

fun Context.confirmationDialog(
    title: String,
    msg: String,
    btnPositiveClick: (() -> Unit)? = null,
    btnNegativeClick: (() -> Unit)? = null
) {
    val mDialog = PrettyDialog(this)
//    mDialog.setTypeface(this.font(R.font.montserrat_regular))
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(R.drawable.ic_information)
        .setTitle(title)
        .setMessage(msg)
        .addButton(getString(R.string.yes), R.color.color_174b71, R.color.color_EDEDED) {
            btnPositiveClick?.invoke()
            mDialog.dismiss()
        }.addButton(getString(R.string.cancel), R.color.color_174b71, R.color.color_EDEDED) {
            btnNegativeClick?.invoke()
            mDialog.dismiss()
        }.show()
}

fun Context.confirmationDialog(
    msg: String,
    btnPositive: String,
    btnNegative: String,
    btnPositiveClick: (() -> Unit)? = null,
    btnNegativeClick: (() -> Unit)? = null,
    icon: Int? = R.drawable.ic_information
) {
    val mDialog = PrettyDialog(this)
//    mDialog.setTypeface(this.font(R.font.montserrat_regular))
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(icon)
        .setMessage(msg)
        .addButton(btnNegative, R.color.color_174b71, R.color.color_EDEDED) {
            btnNegativeClick?.invoke()
            mDialog.dismiss()
        }.addButton(btnPositive, R.color.color_174b71, R.color.color_EDEDED) {
            btnPositiveClick?.invoke()
            mDialog.dismiss()
        }.show()
}

fun Context.confirmationDialog(
    title: String,
    msg: String,
    btnPositive: String,
    btnNegative: String,
    btnPositiveClick: (() -> Unit)? = null,
    btnNegativeClick: (() -> Unit)? = null,
    icon: Int? = R.drawable.ic_information
) {
    val mDialog = PrettyDialog(this)
//    mDialog.setTypeface(this.font(R.font.montserrat_regular))
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(icon)
        .setTitle(title)
        .setMessage(msg)
        .addButton(btnPositive, R.color.color_174b71, R.color.color_EDEDED) {
            btnPositiveClick?.invoke()
            mDialog.dismiss()
        }
        .addButton(btnNegative, R.color.color_174b71, R.color.color_EDEDED) {
            btnNegativeClick?.invoke()
            mDialog.dismiss()
        }.show()
}

fun Context.confirmationDialog(
    title: String,
    msg: String,
    btnPositive: String,
    btnPositiveClick: (() -> Unit)? = null,
    icon: Int? = R.drawable.ic_information
) {
    val mDialog = PrettyDialog(this)
//    mDialog.setTypeface(this.font(R.font.montserrat_regular))
    mDialog.setCanceledOnTouchOutside(false)
    mDialog.setCancelable(false)
    mDialog.setIcon(icon)
        .setTitle(title)
        .setMessage(msg)
        .addButton(btnPositive, R.color.color_174b71, R.color.color_EDEDED) {
            btnPositiveClick?.invoke()
            mDialog.dismiss()
        }.show()
}

fun Context.showListDialog(
    title: String?,
    list: ArrayList<String>,
    onItemSelected: ((item: String) -> Unit)? = null
) {
    val mDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    val data = list.toTypedArray()
    mDialog.setTitle(title)
        .setItems(data) { dialogInterface, which ->
            dialogInterface.dismiss()
            onItemSelected?.invoke(data[which])
        }
        .create().show()
}

fun Context.showListDialog(
    title: String?,
    data: Array<String>,
    onItemSelected: ((item: String) -> Unit)? = null
) {
    val mDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    mDialog.setTitle(title)
        .setItems(data) { dialogInterface, which ->
            dialogInterface.dismiss()
            onItemSelected?.invoke(data[which])
        }
        .create().show()
}

fun Context.showListDialog(
    @StringRes title: Int,
    data: Array<String>,
    onItemSelected: ((item: String, which: Int) -> Unit)? = null
) {
    val mDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    mDialog.setTitle(title)
        .setItems(data) { dialogInterface, which ->
            dialogInterface.dismiss()
            onItemSelected?.invoke(data[which], which)
        }
        .create().show()
}

fun <T> Context.showCustomListDialog(
    title: String?,
    list: ArrayList<T>,
    onItemSelected: ((item: T) -> Unit)? = null
) {
    val mDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    val data: Array<String?> = arrayOfNulls(list.size)
    list.forEachIndexed { index, t -> data[index] = t.toString() }
    mDialog.setTitle(title)
        .setItems(data) { dialogInterface, which ->
            dialogInterface.dismiss()
            onItemSelected?.invoke(list[which])
        }
        .create().show()
}

fun <T> Context.showCustomListDialog(
    @StringRes title: Int,
    list: ArrayList<T>,
    onItemSelected: ((item: T) -> Unit)? = null
) {
    val mDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    val data: Array<String?> = arrayOfNulls(list.size)
    list.forEachIndexed { index, t -> data[index] = t.toString() }
    mDialog.setTitle(title)
        .setItems(data) { dialogInterface, which ->
            dialogInterface.dismiss()
            onItemSelected?.invoke(list[which])
        }
        .create().show()
}

fun Context.showCustomAlert(alertDialog: AlertDialog.Builder.() -> Unit) =
    AlertDialog.Builder(this).apply(alertDialog)

fun loadWebView(
    webViewTerms: WebView?,
    url: String?,
    onPageLoad: () -> Unit
) {
    val settings = webViewTerms?.settings
    settings?.javaScriptEnabled = true
    settings?.loadWithOverviewMode = true
    settings?.useWideViewPort = true
    settings?.builtInZoomControls = false
    settings?.displayZoomControls = false
    webViewTerms?.webChromeClient = WebChromeClient()
    webViewTerms?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

    webViewTerms?.webViewClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onPageLoad.invoke()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
//                webViewTerms?.loadUrl(request?.url.toString())
            return false
        }
    }
    url?.let { webViewTerms?.loadUrl(it) }
}