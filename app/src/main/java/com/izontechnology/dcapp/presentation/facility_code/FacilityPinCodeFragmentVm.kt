package com.izontechnology.dcapp.presentation.facility_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.data.response.ValidCodeResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FacilityPinCodeFragmentVm @Inject constructor(val repository: DeviceInfoRepository) :
    BaseViewModel() {
    @Inject
    lateinit var prefs: SharedPrefs

    var facilityCode = MutableLiveData<String>("")

    var validCodeResponse = MutableLiveData<Resource<ValidCodeResponse>>()

    fun getDownloadInfo(code: String) {
        viewModelScope.launch {
            repository.checkValidCode(code).collect {
                validCodeResponse.value = it
            }
        }
    }

}