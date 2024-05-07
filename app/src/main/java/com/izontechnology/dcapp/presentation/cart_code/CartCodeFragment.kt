package com.izontechnology.dcapp.presentation.cart_code


import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.databinding.FragmentCartCodeBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.http.HTTP_FORBIDDEN

@AndroidEntryPoint
class CartCodeFragment : BaseFragment<FragmentCartCodeBinding, CartCodeFragmentVm>() {

    override fun observeViewModel() {
        viewModel.cartCode.observe(this) {
            enableDisableContBtn(!it.isNullOrEmpty())
        }
        viewModel.downloadInfoResponse.observe(this@CartCodeFragment) {
            handleGetDownloadInfo(it)
        }
    }

    override fun initViewBinding() {
        binding.vm = viewModel
        binding.confirmBtn.setOnClickListener {
            callDownloadInfoAPI()
//            findNavController().navigate(R.id.action_download_application)
        }
    }

    private fun callDownloadInfoAPI() {
        loadingDialog(true)
        viewModel.getDownloadInfo(viewModel.cartCode.value ?: "")
    }

    /**
     * Handles the response of adding a new user address.
     *
     * @param state The resource state containing the response data.
     */
    private fun handleGetDownloadInfo(state: Resource<DownloadInfoResponse>) {
        when (state) {
            is Resource.Success -> {
                loadingDialog(false)
                callDownLoadScreen(state.data)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                loadingDialog(false)
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    viewModel.getToken(onSuccess = {
                        loadingDialog(false)
                        callDownloadInfoAPI()
                    }, onFail = {state,message->
                        loadingDialog(false)
                    })
                }
            }

            is Resource.APIException -> {
                loadingDialog(false)
            }

            is Resource.Idle -> {
                loadingDialog(false)
            }
        }
    }

    private fun callDownLoadScreen(data: DownloadInfoResponse?) {
        findNavController().navigate(R.id.action_download_application, Bundle().apply {
            putString("downloadZipUrl", data?.tileszip)
        })
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