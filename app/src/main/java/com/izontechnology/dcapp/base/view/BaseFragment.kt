package com.izontechnology.dcapp.base.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.databinding.LayoutProgressbarBinding
import com.izontechnology.dcapp.utils.PermissionCallBack
import com.izontechnology.dcapp.utils.toast
import java.lang.reflect.ParameterizedType

const val BACK_PRESS_DELAY = 600L

abstract class BaseFragment<DB : ViewBinding, VM : BaseViewModel> : Fragment() {

    private var hasInitializedRootView = false

    var isBackPressedEnabled = false
    private var permissionCallBack: PermissionCallBack? = null
    private var permissions = ArrayList<String>()
    var dialog: Dialog? = null
    private var dialogBinding: LayoutProgressbarBinding? = null

    protected val backPressCallback: OnBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isBackPressedEnabled) return
                onBackPressed()
            }
        }
    }

    protected val binding: DB by lazy {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.e("Your current page : ", this::class.java.name)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        if (!hasInitializedRootView) {
            initViewBinding()
            observeViewModel()
            hasInitializedRootView = true
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isBackPressedEnabled = true
            backPressCallback.isEnabled = shouldHandleOnBackPressed()
        }, BACK_PRESS_DELAY)
    }

    private fun initData() {
        viewModel.isLoading.observe(
            this.viewLifecycleOwner,
            Observer {
                if (requireActivity() is BaseActivity<*, *>) {
                    (requireActivity() as BaseActivity<*, *>).loadingDialog(
                        viewModel.isLoading.value,
                        viewModel.loadingMessage.value
                    )
                } else {
                    loadingDialog(viewModel.isLoading.value, viewModel.loadingMessage.value)
                }
            }
        )
    }

    /**
     * On back pressed
     *
     * @param navigateUp
     */
    open fun onBackPressed(navigateUp: Boolean = false) {
        val isNavHostFragmentExisted =
            requireActivity().supportFragmentManager.fragments.any { it is NavHostFragment }
        if (isNavHostFragmentExisted && navigateUp && findNavController().navigateUp()) {
            return
        }
        if (isNavHostFragmentExisted && findNavController().popBackStack()) {
            return
        }
        requireActivity().finish()
    }

    /**
     * Should handle on back pressed
     *
     * @return
     */// Determine if this fragment should handle manual back press
    open fun shouldHandleOnBackPressed(): Boolean = false

    protected abstract fun observeViewModel()

    protected abstract fun initViewBinding()

    fun toastMessage(message: String?) {
        message?.let {
            toast(message)
        }
    }

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
                    requireActivity(),
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
        if (requireActivity() is BaseActivity<*, *>) {
            (requireActivity() as BaseActivity<*, *>).loadingDialog(b, message)
        } else {
            if ((b == true)) {
                if ((dialog == null)) {
                    dialogBinding = LayoutProgressbarBinding.inflate(layoutInflater)
                    dialog = AppCompatDialog(requireContext()/*, R.style.progress_bar_style*/)
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
    }

}