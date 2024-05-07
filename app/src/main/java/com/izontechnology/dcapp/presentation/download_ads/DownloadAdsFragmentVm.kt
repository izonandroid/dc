package com.izontechnology.dcapp.presentation.download_ads

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.data.response.MediaDownloadResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadAdsFragmentVm @Inject constructor(val repository: DeviceInfoRepository) : BaseViewModel() {

    var mediaResponse = MutableLiveData<Resource<MediaDownloadResponse>>()
    var downloadInfoResponse = MutableLiveData<Resource<DownloadInfoResponse>>()
    fun getMediaData(code: String) {
        viewModelScope.launch {
            repository.getMediaUrls(code).collect {
                mediaResponse.value = it
            }
        }
    }

    fun getDownloadInfo(code: String) {
        viewModelScope.launch {
            repository.getDownloadUrl(code).collect {
                downloadInfoResponse.value = it
            }
        }
    }

}