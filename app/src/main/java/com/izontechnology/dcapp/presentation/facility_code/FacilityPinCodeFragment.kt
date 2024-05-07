package com.izontechnology.dcapp.presentation.facility_code

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.response.ValidCodeResponse
import com.izontechnology.dcapp.databinding.FragmentFacilityPinCodeBinding
import com.izontechnology.dcapp.utils.FACILITY_JSON_PATH
import com.izontechnology.dcapp.utils.SUCCESS
import com.izontechnology.dcapp.utils.getToken
import com.izontechnology.dcapp.utils.isUpperCase
import com.izontechnology.dcapp.utils.saveJSONFile
import com.izontechnology.dcapp.utils.simpleAlert
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.http.HTTP_FORBIDDEN


@AndroidEntryPoint
class FacilityPinCodeFragment :
    BaseFragment<FragmentFacilityPinCodeBinding, FacilityPinCodeFragmentVm>() {

    override fun observeViewModel() {
        binding.lifecycleOwner = this
        viewModel.facilityCode.observe(this) {
            if (!it.isUpperCase()){
                binding.facilityCode.setText(it.uppercase())
                binding.facilityCode.setSelection(binding.facilityCode.length(),binding.facilityCode.length())
            }
            enableDisableContBtn(!it.isNullOrEmpty())
        }
        viewModel.validCodeResponse.observe(this@FacilityPinCodeFragment) {
            handleGetDownloadInfo(it)
        }
    }

    override fun initViewBinding() {
        binding.vm = viewModel
        binding.imgBack.setOnClickListener { requireActivity().onBackPressed() }
        //TODO uncomment code when share build
        if (viewModel.prefs.getFacilityCode().isNotEmpty()) {
            viewModel.facilityCode.value = viewModel.prefs.getFacilityCode()
            callDownloadInfoAPI()
        }
        binding.confirmBtn.setOnClickListener {
            callDownloadInfoAPI()
//            findNavController().navigate(R.id.action_download_application)
        }
        binding.txtDontCode.setOnClickListener {
            requireContext().simpleAlert(getString(R.string.facility_code_dont_message))
        }
    }

    private fun callDownloadInfoAPI() {
        loadingDialog(true)
        viewModel.getDownloadInfo(viewModel.facilityCode.value ?: "")
    }

    /**
     * Handles the response of adding a new user address.
     *
     * @param state The resource state containing the response data.
     */
    private fun handleGetDownloadInfo(state: Resource<ValidCodeResponse>) {
        when (state) {
            is Resource.Success -> {
                loadingDialog(false)
                viewModel.prefs.saveFacilityCode(viewModel.facilityCode.value?:"")
                callDownLoadScreen(state.data)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    getToken(viewModel.viewModelScope,viewModel.repository,onSuccess = {
//                        loadingDialog(false)
                        callDownloadInfoAPI()
                    }) {
                        loadingDialog(false)
                    }
                }else{
                    loadingDialog(false)
                    toastMessage(state.message)
                }
            }

            is Resource.APIException -> {
                loadingDialog(false)
                toastMessage(state.message)
            }

            is Resource.Idle -> {
                loadingDialog(false)
            }
        }
    }

    private fun callDownLoadScreen(data: ValidCodeResponse?) {
        if (data?.message.equals(SUCCESS,true)) {
            val json = JsonObject()
            json.addProperty("clubcode",data?.clubcode)
            json.addProperty("clubid",data?.clubid)
            saveJSONFile(Gson().toJson(json),FACILITY_JSON_PATH)
            val bundle = Bundle()
            bundle.putString("code",viewModel.facilityCode.value )
            findNavController().navigate(R.id.action_download_application, bundle)
        }else{
            requireContext().simpleAlert(getString(R.string.facility_code_dont_message))
        }
    }

    fun enableDisableContBtn(enable: Boolean = false) {
        binding.confirmBtn?.isEnabled = enable
        if (enable) {
            binding.confirmBtn?.setBackgroundResource(R.drawable.rec_space_gray_cr_8)
        } else {
            binding.confirmBtn?.setBackgroundResource(R.drawable.rec_gray_cr_8)
        }
    }
}
