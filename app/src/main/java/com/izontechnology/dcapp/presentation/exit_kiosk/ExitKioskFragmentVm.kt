package com.izontechnology.dcapp.presentation.exit_kiosk

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class ExitKioskFragmentVm @Inject constructor(val repository: DeviceInfoRepository) : BaseViewModel() {
    var mediaResponse = MutableLiveData<Resource<ResponseBody>>()
//    fun uploadLogFile(fileUpload: MultipartBody.Part?, deviceid: RequestBody?, deviceimei: RequestBody?, filename: RequestBody?,code: RequestBody?, clientTs: RequestBody?) {
//        viewModelScope.launch {
//            repository.uploadFile(fileUpload, deviceid, deviceimei, filename, code,clientTs).collect {
//                mediaResponse.value = it
//            }
//        }
//    }
}