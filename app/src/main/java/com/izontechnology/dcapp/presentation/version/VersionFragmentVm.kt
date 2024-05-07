package com.izontechnology.dcapp.presentation.version

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.data.request.VersionRequest
import com.izontechnology.dcapp.data.response.VersionResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VersionFragmentVm @Inject constructor(val repository: DeviceInfoRepository) :
    BaseViewModel() {
    var versionResponse = MutableLiveData<Resource<VersionResponse>>()
    fun getVersionDetails(versionRequest: VersionRequest) {
        viewModelScope.launch {
            repository.getVersions(versionRequest).collect {
                versionResponse.value = it
            }
        }
    }
}