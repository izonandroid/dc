package com.izontechnology.dcapp.base.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.databinding.LayoutProgressbarBinding
import com.izontechnology.dcapp.utils.PermissionCallBack
import com.izontechnology.dcapp.utils.log.HyperLog
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<DB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {
    private var permissionCallBack: PermissionCallBack? = null
    private var permissions = ArrayList<String>()
    private var dialog: Dialog? = null
    private var dialogBinding: LayoutProgressbarBinding? = null

    val binding: DB by lazy {
        val persistentClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<DB>
        val inflateMethod = persistentClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        inflateMethod.invoke(null, layoutInflater) as DB
    }

    protected val viewModel: VM by lazy {
        val persistentClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[1] as Class<VM>
        ViewModelProvider(this)[persistentClass]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initThemeData()
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash)
        BaseApplication.activity = this
        Log.e("exit kiosk", "base ${this}")
        setContentView(binding.root)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initData()
        initViewBinding()
        observeViewModel()
    }

    private fun initData() {
        viewModel.isLoading.observe(
            this,
            Observer {
                loadingDialog(viewModel.isLoading.value, viewModel.loadingMessage.value)
            }
        )
    }

    private val handleAppCrash =
        Thread.UncaughtExceptionHandler { thread, ex ->
            Log.e("error", ex.toString())
            HyperLog.exception("error", ex.message, ex)
        }


    protected abstract fun observeViewModel()

    protected abstract fun initViewBinding()

    fun requestPermission(
        permissions: ArrayList<String>,
        permissionCallBack: PermissionCallBack?
    ) {
        this.permissionCallBack = permissionCallBack
        this.permissions = permissions
        mPermissionResult.launch(permissions.toTypedArray())
    }

    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.forEach { actionMap ->
            val granted = actionMap.value
            val permission = actionMap.key
            if (!granted) {
                val neverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
                )
                if (neverAskAgain) {
                    permissionCallBack?.onPermissionDisabled()
                } else {
                    permissionCallBack?.permissionDenied()
                }
            } else {
                permissionCallBack?.permissionGranted()
            }
            return@registerForActivityResult
        }
    }

    /**
     * Display progress dialog on screen when this method call with true flag
     * Dismiss progress dialog if dialog isShowing and flag is false
     * @param b     Dialog display control flag
     */
    fun loadingDialog(b: Boolean?, message: String? = getString(R.string.loading)) {
        if ((b == true)) {
            if ((dialog == null)) {
                dialogBinding = LayoutProgressbarBinding.inflate(layoutInflater)
                dialog = AppCompatDialog(this/*, R.style.progress_bar_style*/)
                dialogBinding?.root?.let { dialog?.setContentView(it) }
                dialog?.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog?.setCancelable(false)
                dialog?.setCanceledOnTouchOutside(false)
            }
            dialogBinding?.message?.text = message
            dialog?.show()
        } else {
            if (dialog?.isShowing == true)
                dialog?.dismiss()
        }
    }

    /** This method is used to set theme for dark and light mode */
    fun initThemeData() {
        when (SharedPrefs(this).getManualTheme()) {
            0 -> {
//                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            1 -> {
//                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            else -> {
//                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}