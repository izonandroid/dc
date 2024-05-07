package com.izontechnology.dcapp.presentation.cart_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.request.UserRequest
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartCodeFragmentVm @Inject constructor(private val repository: DeviceInfoRepository) :
    BaseViewModel() {
    @Inject
    lateinit var prefs: SharedPrefs

    var cartCode = MutableLiveData<String>("")

    var downloadInfoResponse = MutableLiveData<Resource<DownloadInfoResponse>>()

    fun getDownloadInfo(code: String) {
        viewModelScope.launch {
            repository.getDownloadUrl(code).collect {
                downloadInfoResponse.value = it
            }
        }
    }

    fun getToken(onSuccess: () -> Unit, onFail: (status: String?, message: String?) -> Unit) {
        val user = UserRequest("tqVsR7KHqfyqfiMpKLXJ", "dcappuser")
        viewModelScope.launch {
            repository.getToken(user).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.token?.let { it1 -> prefs.saveToken("bearer $it1") }
                        onSuccess.invoke()
                    }

                    is Resource.Error -> {
                        onFail.invoke(it.status, it.message)
                    }

                    is Resource.APIException -> {
                        onFail.invoke(it.status.toString(), it.message)
                    }

                    else -> {
                        onFail.invoke(null, null)
                    }
                }
            }
        }
    }
}