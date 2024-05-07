package com.izontechnology.dcapp.presentation.sim

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.databinding.FragmentSimCardActivationBinding
import com.izontechnology.dcapp.utils.DARK_THEME
import com.izontechnology.dcapp.utils.LIGHT_THEME
import com.izontechnology.dcapp.utils.setGradientTextColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SimCardActivationFragment :
    BaseFragment<FragmentSimCardActivationBinding, SimCardActivationFragmentVm>() {

    @Inject
    lateinit var pref: SharedPrefs

    override fun observeViewModel() {
        viewModel.progress.observe(this) {
            binding.txtProgress?.text = (it / 1000).toString()
        }
        viewModel.setTimer {
            findNavController().navigate(R.id.action_download_application,arguments?: Bundle.EMPTY)
        }
    }

    override fun initViewBinding() {
        setupUI()
    }


    private fun setupUI() {
        when (pref.getManualTheme()) {
            LIGHT_THEME -> {
                binding.txtMessage.setGradientTextColor(
                    R.color.color_loading_text,
                    R.color.color_loading_text
                )
            }

            DARK_THEME -> {
                binding.txtMessage.setGradientTextColor(R.color.white, R.color.white_50)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.cTimer?.cancel()
        viewModel.cTimer = null
    }

}