package com.izontechnology.dcapp.presentation.version

import android.app.DownloadManager
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.common.ScannedWifiItem
import com.izontechnology.dcapp.data.request.VersionRequest
import com.izontechnology.dcapp.data.response.Availablenetwork
import com.izontechnology.dcapp.data.response.NetworkType
import com.izontechnology.dcapp.data.response.VersionResponse
import com.izontechnology.dcapp.databinding.FragmentVersionBinding
import com.izontechnology.dcapp.downloadmanagerplus.FileItem
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadStatus
import com.izontechnology.dcapp.downloadmanagerplus.enums.Errors
import com.izontechnology.dcapp.downloadmanagerplus.enums.Storage
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.ActionListener
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.DownloadListener
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem
import com.izontechnology.dcapp.downloadmanagerplus.utils.Utils
import com.izontechnology.dcapp.presentation.host_activity.HostActivityVM
import com.izontechnology.dcapp.utils.DARK_THEME
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.HUNDRED
import com.izontechnology.dcapp.utils.LIGHT_THEME
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.connectToWifi
import com.izontechnology.dcapp.utils.executeAsyncTask
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getAvailableNetwork
import com.izontechnology.dcapp.utils.getConnectedNetworkType
import com.izontechnology.dcapp.utils.getDownloadAllowNetwork
import com.izontechnology.dcapp.utils.getToken
import com.izontechnology.dcapp.utils.installApplication
import com.izontechnology.dcapp.utils.isDownloadSkip
import com.izontechnology.dcapp.utils.isFileDownloaded
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.setGradientTextColor
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.http.HTTP_FORBIDDEN
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class VersionFragment : BaseFragment<FragmentVersionBinding, VersionFragmentVm>() {
    var izonAppVersion = "0.0.0"
    var versionResponse: VersionResponse? = null
    private val mainVM: HostActivityVM by activityViewModels()
    var retryCount = 0

    var dcAppVersion = BuildConfig.VERSION_NAME
//    var dcAppVersion = "1.1.0"

    @Inject
    lateinit var prefs: SharedPrefs

    override fun observeViewModel() {
//        enableDisableBtn(false,binding.btnConfirm)
        enableDisableBtn(false, binding.btnIzonUpdate)
        enableDisableBtn(false, binding.btnIzonDcUpdate)
        izonAppVersion = try {
            requireContext().packageManager?.getPackageInfo(izonPackageName, 0)?.versionName
                ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
//        izonAppVersion = "1.0.0"
        callVersionInfoAPI()
        viewModel.versionResponse.observe(this) {
            handleGetVersionInfo(it)
        }
    }

    override fun initViewBinding() {
        setupUI()
        binding.btnConfirm.setOnClickListener {
            callNextScreen()
        }
        binding.btnIzonDcUpdate.setOnClickListener {
            enableDisableBtn(false, binding.btnConfirm)
            enableDisableBtn(false, binding.btnIzonUpdate)
            enableDisableBtn(false, binding.btnIzonDcUpdate)
            if (versionResponse != null) {
                if (!versionResponse?.dcapp?.version.equals(dcAppVersion)) {
                    startDownload(
                        versionResponse?.dcapp?.app + versionResponse?.dcapp?.version,
                        versionResponse?.dcapp?.downloadlink ?: "",
                        binding.txtDCMessage,
                        requireContext().packageName,
                        versionResponse?.dcapp?.version,
                        versionResponse?.izonapp?.networktype
                    )
                }
            } else {
                callVersionInfoAPI()
            }
        }
        binding.btnIzonUpdate.setOnClickListener {
            enableDisableBtn(false, binding.btnConfirm)
            enableDisableBtn(false, binding.btnIzonDcUpdate)
            enableDisableBtn(false, binding.btnIzonUpdate)
            if (versionResponse != null) {
                if (!versionResponse?.izonapp?.version.equals(izonAppVersion)) {
                    startDownload(
                        versionResponse?.izonapp?.app + versionResponse?.izonapp?.version,
                        versionResponse?.izonapp?.downloadlink ?: "",
                        binding.txtIzonMessage,
                        izonPackageName,
                        versionResponse?.izonapp?.version,
                        versionResponse?.izonapp?.networktype

                    )
                }
            } else {
                callVersionInfoAPI()
            }
        }
    }

    private fun callNextScreen() {
//        if (!prefs.getIsWifiOn() and viewModel.wifiManager.isWifiEnabledValue){
//            viewModel.wifiManager.closeWiFi(requireActivity())
//        }
        loadingDialog(false)
        findNavController().navigate(R.id.action_facility_pin_code)
    }

    private fun callVersionInfoAPI() {
        val versionRequest = VersionRequest(
            deviceid = requireContext().getAndroidId(),
            dcappversion = dcAppVersion,
//            dcappversion = "1.1.0",
//            izonappversion = izonAppVersion
            izonappversion = izonAppVersion
        )
        viewModel.getVersionDetails(versionRequest)
    }

    private fun handleGetVersionInfo(state: Resource<VersionResponse>) {
        when (state) {
            is Resource.Success -> {
                enableDisableBtn(true, binding.btnConfirm)
                loadingDialog(false)
                versionResponse = state.data
                val data = state.data
                if (!data?.dcapp?.version.equals(dcAppVersion)) {
                    enableDisableBtn(false, binding.btnConfirm)
                    binding.llDcUpdate.visibility = View.VISIBLE
                    binding.txtDCTitle.setText("${getString(R.string.dc_update_message)} ${data?.dcapp?.version}")
                    enableDisableBtn(true, binding.btnIzonDcUpdate)
                }
                if (!data?.izonapp?.version.equals(izonAppVersion)) {
                    enableDisableBtn(false, binding.btnConfirm)
                    binding.llGolfUpdate.visibility = View.VISIBLE
                    binding.txtGolfTitle.setText("${getString(R.string.izon_golf_update_message)} ${data?.izonapp?.version}")
                    enableDisableBtn(true, binding.btnIzonUpdate)
                }
                if (!data?.dcapp?.version.equals(dcAppVersion)) {
                    startDownload(
                        data?.dcapp?.app + data?.dcapp?.version,
                        data?.dcapp?.downloadlink ?: "",
                        binding.txtDCMessage,
                        requireContext().packageName,
                        data?.dcapp?.version,
                        data?.dcapp?.networktype
                    )
                } else if (!data?.izonapp?.version.equals(izonAppVersion)) {
                    startDownload(
                        data?.izonapp?.app + data?.izonapp?.version,
                        data?.izonapp?.downloadlink ?: "",
                        binding.txtIzonMessage,
                        izonPackageName,
                        data?.izonapp?.version,
                        data?.izonapp?.networktype
                    )
                }
                if ((data?.izonapp?.version.equals(izonAppVersion)) and (data?.dcapp?.version.equals(
                        dcAppVersion
                    ))
                ) {
                    enableDisableBtn(true, binding.btnConfirm)
                    callNextScreen()
                }
                setupUI()
                var keepFileNames = arrayListOf<String>(
                    Utils.getFileName(data?.izonapp?.downloadlink),
                    Utils.getFileName(data?.dcapp?.downloadlink),
                )
                deleteOthersFile(keepFileNames)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                enableDisableBtn(true, binding.btnConfirm)
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    getToken(viewModel.viewModelScope, viewModel.repository, onSuccess = {
//                        loadingDialog(false)
                        callVersionInfoAPI()
                    }) {
                        loadingDialog(false)
                    }
                } else {
                    loadingDialog(false)
                    toastMessage(state.message)
                }
            }

            is Resource.APIException -> {
                enableDisableBtn(true, binding.btnConfirm)
                loadingDialog(false)
                toastMessage(state.message)
            }

            is Resource.Idle -> {
                loadingDialog(false)
            }
        }
    }

    fun enableDisableBtn(enable: Boolean = false, button: Button) {
        button?.isEnabled = enable
        if (enable) {
            button?.setBackgroundResource(R.drawable.rec_space_gray_cr_8)
        } else {
            button?.setBackgroundResource(R.drawable.rec_gray_cr_8)
        }
    }

    private fun startDownload(
        token: String,
        link: String,
        message: TextView,
        packageName: String,
        version: String?,
        networktype: NetworkType?
    ) {
        /*if (requireContext().getConnectedNetworkType().equals("sim") and (retryCount < 3)) {
            if (prefs.getIsWifiOn()) {
                activity?.let { mainVM.wifiManager.openWiFi(it) }
            }
            getAvailableNetwork(
                viewModel.viewModelScope,
                viewModel.repository,
                prefs.getFacilityCode(),
                { response ->
                    val availableWifi: ArrayList<Availablenetwork?>? = ArrayList()
                    val availableWifiResult: ArrayList<ScanResult?>? = ArrayList()
                    response?.availablenetworks?.add(
                        Availablenetwork(
                            ssid = "Yagnik",
                            password = "7984768525"
                        )
                    )
                    response?.availablenetworks?.add(
                        Availablenetwork(
                            ssid = "Yagnik_1",
                            password = "7984768525"
                        )
                    )
                    mainVM.wifiNetworkList.value?.forEach { result ->
                        val wifi =
                            response?.availablenetworks?.firstOrNull { it.ssid.equals(result?.SSID) }
                        if (wifi != null) {
                            availableWifi?.add(wifi)
                            availableWifiResult?.add(result)
                        }
                    }
                    if (availableWifi?.isEmpty() != true and (prefs.getIsWifiOn())) {
                        tryToConnectWifi(availableWifi, availableWifiResult, {
                            loadingDialog(false);
                            startDownload(
                                token,
                                link,
                                message,
                                packageName,
                                version,
                                networktype
                            )
                        }, {
                            retryCount = 3
                            loadingDialog(false);
                            startDownload(
                                token,
                                link,
                                message,
                                packageName,
                                version,
                                networktype
                            )
                        })
                    } else {
                        retryCount = 3
                        loadingDialog(false);
                        startDownload(
                            token,
                            link,
                            message,
                            packageName,
                            version,
                            networktype
                        )
                    }
                },
                {
                    loadingDialog(false);
                    startDownload(token, link, message, packageName, version, networktype)
                })
            retryCount++
        }*/
        /*else if (networktype?.isDownloadSkip(requireContext()) == true) {
            callNextScreen()
        } */
//        else {
            val currentItem = FileItem()
//            val link = "https://getsamplefiles.com/download/zip/sample-1.zip"
            currentItem.token = token
            currentItem.uri = link
            currentItem.packageName = packageName
            currentItem.version = version
            currentItem.networkType = networktype
            lifecycleScope.executeAsyncTask(onPreExecute = {
            }, doInBackground = {
                try {
                    val fileLength = URL(link).openConnection().contentLength
                    currentItem.fileSize = fileLength
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, onPostExecute = {
                if (!requireContext().isFileDownloaded(
                        currentItem,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/Version/" + Utils.getFileName(
                            currentItem.getUri()
                        )
                    )
                ) {
                    try {
                        val downloaderItem =
                            Downloader.getDownloadItem(requireContext(), currentItem.token)
                        if (downloaderItem.uri.equals(link)) {
                            downLoadFile(currentItem, message)
                        } else {
                            Downloader.getInstance(requireContext())
                                .deleteFile(currentItem.token, object : ActionListener {
                                    override fun onSuccess() {
                                        downLoadFile(currentItem, message)
                                    }

                                    override fun onFailure(error: Errors?) {
                                        downLoadFile(currentItem, message)
                                    }
                                })
                        }
                    } catch (e: Exception) {
                        downLoadFile(currentItem, message)
                    }
                } else {
                    message.text = getString(R.string.download_completed)
                    val filePath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/Version/" + Utils.getFileName(
                            currentItem.getUri()
                        )
                    installAndUpdateButton(
                        File(filePath),
                        currentItem.packageName,
                        currentItem.version
                    )
                }
            })
//        }
    }

    private fun tryToConnectWifi(
        availableWifi: ArrayList<Availablenetwork?>?,
        availableWifiResult: ArrayList<ScanResult?>?,
        connected: () -> Unit,
        notValidWifi: () -> Unit
    ) {
        if (availableWifi?.isEmpty() == true) {
            notValidWifi.invoke()
        } else {
            connectToWifi(
                ScannedWifiItem(availableWifiResult?.get(0)),
                availableWifi?.get(0)?.password,
                viewModel.wifiManager,
                mainVM,
                { loadingDialog(true) },
                { connected.invoke() },
                {
                    availableWifi?.removeAt(0)
                    availableWifiResult?.removeAt(0)
                    tryToConnectWifi(availableWifi, availableWifiResult, connected, notValidWifi)
                }
            )
        }
    }

    private fun installAndUpdateButton(
        file: File,
        packageName: String,
        version: String?
    ) {
//        if (packageName.equals(requireContext().packageName)){
        loadingDialog(true)
//        }
//        if (packageName.equals(requireContext().packageName)) {
//            Handler(Looper.getMainLooper()).postDelayed({
//                requireActivity().runOnUiThread {
//                    if (packageName.equals(requireContext().packageName)) {
//                        LegacyUtils.reboot(requireContext())
//                    }
//                }
//            }, 100)
//        }
        if (packageName.equals(requireContext().packageName)) {
            installApplication(
                requireContext(),
                file,
                packageName,
                version
            )
            LegacyUtils.reboot(requireContext())
        } else {
            installApplication(
                requireContext(),
                file,
                packageName,
                version
            )
        }
        Thread.sleep(1000)
        binding.txtDCMessage.text = ""
        binding.txtIzonMessage.text = ""
        enableDisableBtn(true, binding.btnConfirm)
        enableDisableBtn(true, binding.btnIzonDcUpdate)
        enableDisableBtn(true, binding.btnIzonUpdate)
        callNextScreen()
    }

    fun deleteOthersFile(keepFileNames: ArrayList<String>) {
        val dir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/Version/")
        dir.listFiles()?.forEach {
            if (!keepFileNames.contains(it.name)) {
                it.delete()
            }
        }
    }

    private fun downLoadFile(item: FileItem, message: TextView) {
        var downloader = getDownloader(item, item.listener)
        if (downloader?.getStatus(item.getToken()) === DownloadStatus.RUNNING || downloader?.getStatus(
                item.getToken()
            ) === DownloadStatus.PAUSED || downloader?.getStatus(item.getToken()) === DownloadStatus.PENDING
        ) {
            if (downloader?.getStatus(item.getToken()) === DownloadStatus.PENDING
                || (downloader?.downloadedBytes ?: 0) <= 0
            ) {
                downloader?.cancel(item.getToken())
            } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.PAUSED) {
                //int status = Downloader.resume(this, item.getToken());
                downloader?.resume()
            } else {
                //int status = Downloader.pause(this, item.getToken());
                downloader?.pause()
            }
        } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.SUCCESSFUL) {
            try {
                downloader?.deleteFile(item?.token, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            downloader?.start()
            DeviceLogs.e("download", "download successfull")
        } else {
            downloader?.start()
        }
        if (item.listener == null) {
            item.listener = getDownloadListener(
                item,
                message
            )
        }
        //Showing progress for running downloads.
        showProgress(item, item.listener)
    }

    private fun showProgress(item: FileItem, listener: DownloadListener?) {
        getDownloader(item, listener)?.showProgress()
    }

    private fun getDownloadListener(
        item: FileItem,
        tvPercent: TextView,
    ): DownloadListener {
        return object : DownloadListener {
            var lastStatus = DownloadStatus.NONE
            var lastPercent = 0
            override fun onComplete(
                totalBytes: Int,
                mDownloadItem: DownloadItem?,
                downloadUri: String?
            ) {
                item.downloadStatus = DownloadStatus.SUCCESSFUL
                tvPercent.setText("$HUNDRED%")
                tvPercent.text = getString(R.string.download_completed)
                tvPercent.text = getString(R.string.install_apk)
                if (lastStatus !== DownloadStatus.SUCCESSFUL) {
                    if (downloadUri.isNullOrEmpty()) {
                        mDownloadItem?.filePath?.let {
                            tvPercent.text = getString(R.string.install_apk)
                            installAndUpdateButton(
                                File(it),
                                item.packageName,
                                item.version
                            )
                        }
                    } else {
                        tvPercent.text = getString(R.string.install_apk)
                        Uri.parse(downloadUri).path?.let {
                            installAndUpdateButton(
                                File(it),
                                item.packageName,
                                item.version
                            )
                        }
                    }
                }
                lastStatus = DownloadStatus.SUCCESSFUL

            }

            override fun onPause(
                percent: Int,
                reason: DownloadReason,
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.PAUSED) {
                    item.downloadStatus = DownloadStatus.PAUSED
                }
                if (reason == DownloadReason.PAUSED_WAITING_FOR_NETWORK) {
//                    txtPauseReason.visibility = View.VISIBLE
//                    txtPauseReason.text = getString(R.string.no_internet_waiting_for_network)
                } else {
//                    txtPauseReason.visibility = View.GONE
                }
                tvPercent.setText("$percent%")
                lastStatus = DownloadStatus.PAUSED
            }

            override fun onPending(
                percent: Int,
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.PENDING) {
                    item.downloadStatus = DownloadStatus.PENDING
                }
//                txtPauseReason.visibility = View.GONE
                tvPercent.setText("$percent%")
                lastStatus = DownloadStatus.PENDING
            }

            override fun onFail(
                percent: Int,
                reason: DownloadReason,
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                //Toast.makeText(NormalActivity.this, "Failed: " + reason, Toast.LENGTH_SHORT).show();
                if (lastStatus !== DownloadStatus.FAILED) {
                    item.downloadStatus = DownloadStatus.FAILED
                }
                tvPercent.setText("$percent%")
//                txtPauseReason.visibility = View.GONE
                lastStatus = DownloadStatus.FAILED
            }

            override fun onCancel(
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.CANCELED) {
                    item.downloadStatus = DownloadStatus.CANCELED
                }
                tvPercent.setText("${getString(R.string.downloading_software)}")
//                txtPauseReason.visibility = View.GONE
                lastStatus = DownloadStatus.CANCELED
            }

            override fun onRunning(
                percent: Int,
                totalBytes: Int,
                downloadedBytes: Int,
                downloadSpeed: Float,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.RUNNING) {
                    item.downloadStatus = DownloadStatus.RUNNING
                }
//                txtPauseReason.visibility = View.GONE
                if (percent > lastPercent) {
                    lastPercent = percent
                    tvPercent.setText("$percent%")
//                    var remainTime = ((totalBytes - downloadedBytes) / (downloadSpeed * 1000)).toLong()
//                    if ((remainTime >= 0) and (remainTime <= 86400)) {
//                        txtRemainingTime.text =
//                            "${getString(R.string.remaining)}: ${remainTime.formatTime()}"
//                    } else {
//                        binding.txtRemainingTime.text = getString(R.string.remaining_calculating)
//                    }
                }
                lastStatus = DownloadStatus.RUNNING
            }
        }
    }

    private fun getDownloader(item: FileItem, listener: DownloadListener?): Downloader? {
        val request: Downloader = Downloader.getInstance(requireActivity())
            .setListener(listener)
            .setUrl(item.getUri())
            .setToken(item.getToken())
            .setKeptAllDownload(false) //if true: canceled download token keep in database
            .setAllowedOverRoaming(true)
            .setVisibleInDownloadsUi(true)
            .setScanningByMediaScanner(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedNetworkTypes(
                item.networkType.getDownloadAllowNetwork() ?: DownloadManager.Request.NETWORK_WIFI
            )
//             .setCustomDestinationDir(Storage.DIRECTORY_DOWNLOADS, Utils.getFileName(item.getUri()))//TargetApi 28 and lower
            .setDestinationDir(
                Storage.DIRECTORY_DOWNLOADS, "/Version/" + Utils.getFileName(item.getUri())
            )
            .setNotificationTitle("Downloading")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true) //Api 16 and higher
        }
        return request
    }

    private fun setupUI() {
        when (prefs.getManualTheme()) {
            LIGHT_THEME -> {
                binding.txtDCTitle.setGradientTextColor(
                    R.color.color_loading_text,
                    R.color.color_loading_text
                )
                binding.txtGolfTitle.setGradientTextColor(
                    R.color.color_loading_text,
                    R.color.color_loading_text
                )
            }

            DARK_THEME -> {
                binding.txtDCTitle.setGradientTextColor(R.color.white, R.color.white_50)
                binding.txtGolfTitle.setGradientTextColor(R.color.white, R.color.white_50)
            }
        }
    }
}
