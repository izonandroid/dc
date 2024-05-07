package com.izontechnology.dcapp.services

import android.accessibilityservice.AccessibilityService
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.izontechnology.dcapp.base.common.SharedPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessiblityService : AccessibilityService() {
    private var volumeDownPressed = false
    private val handler = Handler(Looper.getMainLooper())
    var mDialog: Dialog? = null

    @Inject
    lateinit var prefs: SharedPrefs
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "service is connected")
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {

    }

    override fun onInterrupt() {}

    // here you can intercept the keyevent
    override fun onKeyEvent(event: KeyEvent): Boolean {
        return handleKeyEvent(event)
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        if (action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    volumeDownPressed = true
                    handler?.removeCallbacksAndMessages(null)
//                    handler?.postDelayed({
//                        if (volumeDownPressed and (!(mDialog?.isShowing == true)) and (prefs.getIsKiosk())) {
//                            mDialog = applicationContext().simpleInputDialog(
//                                getString(R.string.enter_a_pin_to_exit_kiosk_mode), onOK = { dialog, pin, view ->
//                                    view?.hideSoftKeyboard()
//                                    if (pin.isEmpty()) {
//                                        toastMessage(getString(R.string.please_enter_pin))
//                                    } else  {
//                                        validateKioskCode(viewModel.viewModelScope,viewModel.repository,pin, onSuccess = {
//                                            LegacyUtils.lockDownApp(requireContext(), false, finishActivity = true)
//                                            dialog.dismiss()
//                                        }, onFail = {
//                                            toastMessage(getString(R.string.please_enter_correct_pin))
//                                            requireContext().simpleAlertOverLay(it.toString())
//                                        })
//                                    }
//                                }, onCancel = { dialog, pin, view ->
//                                    installOrLaunch()
//                                })
//                            mDialog = applicationContext.simpleInputDialog(
//                                getString(R.string.enter_a_pin_to_exit_kiosk_mode),
//                                onOK = { dialog, pin, view ->
//                                    view?.hideSoftKeyboard()
//                                    if (pin.isEmpty()) {
//                                        toastMessage("Please Enter pin")
//                                    } else if (pin.equals("777777")) {
//                                        LegacyUtils.lockDownApp(this, false)
//                                        dialog.dismiss()
//                                    } else {
//                                        toastMessage("Please Enter correct pin")
//                                    }
//                                })
//                            volumeDownPressed = false
//                        }
//                    }, 2000)
                    return prefs.getIsKiosk()
                }

                KeyEvent.KEYCODE_VOLUME_UP -> {
                    return prefs.getIsKiosk()
                }
            }
        }
        if (action == KeyEvent.ACTION_UP) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    volumeDownPressed = true
                    handler.removeCallbacksAndMessages(null)
                    return prefs.getIsKiosk()
                }

                KeyEvent.KEYCODE_VOLUME_UP -> {
                    //do something
                    return prefs.getIsKiosk()
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}