package com.izontechnology.dcapp.presentation.kiosk

import androidx.navigation.fragment.findNavController
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.databinding.FragmentKioskBinding
import com.izontechnology.dcapp.utils.LegacyUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class KioskFragment : BaseFragment<FragmentKioskBinding, KioskFragmentVm>() {

    @Inject
    lateinit var prefs: SharedPrefs

    override fun observeViewModel() {

    }

    override fun initViewBinding() {
        if (prefs.getIsKiosk()){
            binding.btnKiosk.setText(R.string.disable_kiosk)
        }else{
            binding.btnKiosk.setText(R.string.enable_kiosk)
        }
        binding.btnKiosk?.setOnClickListener {
            if (LegacyUtils.isDeviceOwner(requireContext())) {
                if (!prefs.getIsKiosk()) {
                    binding.btnKiosk.setText(R.string.disable_kiosk)
                    prefs.setIsKiosk(true)
                    LegacyUtils.lockDownApp(requireContext(),true)
                } else {
                    binding.btnKiosk.setText(R.string.enable_kiosk)
                    prefs.setIsKiosk(false)
                    LegacyUtils.lockDownApp(requireContext(),false)
                }
            } else {
                toastMessage("Please make this application device owner")
            }
        }
        binding.confirmButton.setOnClickListener {
            findNavController().navigate(R.id.action_download_application)
        }
    }

}