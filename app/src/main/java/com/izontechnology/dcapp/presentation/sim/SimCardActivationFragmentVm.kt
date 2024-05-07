package com.izontechnology.dcapp.presentation.sim

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.izontechnology.dcapp.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimCardActivationFragmentVm @Inject constructor(
): BaseViewModel() {
    var cTimer: CountDownTimer? = null
    var progress = MutableLiveData<Long>()
    fun setTimer(onFinish: () -> Unit) {
        if (cTimer == null) {
            cTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    progress.value = millisUntilFinished
                }

                override fun onFinish() {
                    onFinish.invoke()
                }
            }
            cTimer?.start()
        }
    }
}