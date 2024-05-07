package com.izontechnology.dcapp.presentation.downloadapp

import android.app.Dialog
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.common.ScannedWifiItem
import com.izontechnology.dcapp.data.request.ExitKioskRequest
import com.izontechnology.dcapp.data.response.Availablenetwork
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.data.response.MediaDownloadResponse
import com.izontechnology.dcapp.data.response.MediaDownloadResponseItem
import com.izontechnology.dcapp.databinding.FragmentDownloadApplicationBinding
import com.izontechnology.dcapp.downloadmanagerplus.FileItem
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadStatus
import com.izontechnology.dcapp.downloadmanagerplus.enums.Errors
import com.izontechnology.dcapp.downloadmanagerplus.enums.Storage
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.ActionListener
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.DownloadListener
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem
import com.izontechnology.dcapp.downloadmanagerplus.utils.Constants
import com.izontechnology.dcapp.downloadmanagerplus.utils.Utils
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import com.izontechnology.dcapp.presentation.host_activity.HostActivityVM
import com.izontechnology.dcapp.utils.API_RETURN_UPDATE_FORMAT
import com.izontechnology.dcapp.utils.DARK_THEME
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.HUNDRED
import com.izontechnology.dcapp.utils.LIGHT_THEME
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.TILE_JSON_PATH
import com.izontechnology.dcapp.utils.UnZipUtil
import com.izontechnology.dcapp.utils.connectToWifi
import com.izontechnology.dcapp.utils.executeAsyncTask
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getDownloadAllowNetwork
import com.izontechnology.dcapp.utils.getIccId
import com.izontechnology.dcapp.utils.getToken
import com.izontechnology.dcapp.utils.getUsedMemorySize
import com.izontechnology.dcapp.utils.hideSoftKeyboard
import com.izontechnology.dcapp.utils.installApplication
import com.izontechnology.dcapp.utils.isFileDownloaded
import com.izontechnology.dcapp.utils.isPackageInstalled
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.loadJSONFromFile
import com.izontechnology.dcapp.utils.log.HyperLog
import com.izontechnology.dcapp.utils.saveJSONFile
import com.izontechnology.dcapp.utils.setGradientTextColor
import com.izontechnology.dcapp.utils.simpleAlertOverLay
import com.izontechnology.dcapp.utils.simpleInputDialog
import com.izontechnology.dcapp.utils.todayDate
import com.izontechnology.dcapp.utils.validateKioskCodeV2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import okhttp3.internal.http.HTTP_FORBIDDEN
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class DownloadApplicationFragment :
    BaseFragment<FragmentDownloadApplicationBinding, DownloadAppicationFragmentVm>() {

    val TAG = DownloadApplicationFragment::class.java.name

    @Inject
    lateinit var prefs: SharedPrefs

    var downloadId: Long = -1
    var downloader: Downloader? = null
    var currentItem = FileItem()
    var isRefresed = false

    var mediaTotalBytes = 0
    var mediaDownloadedBytes = 0
    var mDialog: Dialog? = null

    var isPauseFirstTime = false
    var retryCount = 0
    private val mainVM: HostActivityVM by activityViewModels()
    var zipExtraction: Job? = null

    override fun onBackPressed(navigateUp: Boolean) {
        super.onBackPressed(navigateUp)

    }

    override fun observeViewModel() {
        viewModel.mediaResponse.observe(this@DownloadApplicationFragment) {
            handleGetMediaInfo(it)
        }
    }

    override fun initViewBinding() {
        setupUI()
        binding.swpRefresh.setOnRefreshListener {
            refreshDonwload()
        }
        viewModel.downloadInfoResponse.observe(this) {
            handleGetDownloadInfo(it)
        }
        downloadId = prefs.getDownloadId()
//        startDownload()
        callDownloadInfoAPI()
    }

    private fun refreshDonwload() {
        //            clearAllDownloadThread()
        zipExtraction?.cancel(null)
        try {
            if (downloader?.getStatus(currentItem.getToken()) === DownloadStatus.SUCCESSFUL) {
                downloader?.deleteFile(currentItem.getToken(), null)
            } else {
                downloader?.cancel(currentItem.getToken())
            }
            currentItem.listener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRefresed = false
//            startDownload()
        clearAllDownloadThread()
        callDownloadInfoAPI()
        binding.swpRefresh.isRefreshing = false
    }

    private fun callDownloadInfoAPI() {
        if (arguments?.containsKey("code") == true) {
            loadingDialog(true)
            viewModel.getDownloadInfo(arguments?.getString("code", "") ?: "")
        }
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
                arguments?.putString("downloadZipUrl", state.data?.tileszip)
                retryCount = 0
                startDownload(state.data)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    getToken(viewModel.viewModelScope, viewModel.repository, onSuccess = {
//                        loadingDialog(false)
                        callDownloadInfoAPI()
                    }) {
                        loadingDialog(false)
                    }
                } else {
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

    private fun clearAllDownloadThread() {
        try {
            if (!Constants.fieldList.isNullOrEmpty()) {
                Utils.removeFromThreadList(Constants.fieldList.get(0))
                clearAllDownloadThread()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startDownload(data: DownloadInfoResponse?) {
        val savedData = try {
            Gson().fromJson(loadJSONFromFile(TILE_JSON_PATH), DownloadInfoResponse::class.java)
        } catch (e: Exception) {
            prefs.getTilesData()
        }
        binding.txtMessage.text = getString(R.string.downloading_software)
        if (arguments?.containsKey("downloadZipUrl") == true) {
            val link = arguments?.getString("downloadZipUrl")
//            val link = "https://getsamplefiles.com/download/zip/sample-1.zip"
            currentItem.token = "id1252"
            currentItem.uri = link
            currentItem.fileSize = data?.filesize ?: 0
            currentItem.networkType = data?.networktype
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
                                startDownload(data)
                            }, {
                                retryCount = 3
                                loadingDialog(false);
                                startDownload(data)
                            })
                        } else {
                            retryCount = 3
                            loadingDialog(false);
                            startDownload(data)
                        }
                    },
                    {
                        loadingDialog(false);
                        startDownload(data)
                    })
                retryCount++
            }*/
            /*else if (data?.networktype?.isDownloadSkip(requireContext()) == true) {
                binding.txtProgress.text = "${HUNDRED}%"
                binding.txtRemainingTime.text = getString(R.string.download_completed)
                callMediaInfoData()
            } */
//            else
            if (savedData?.clubid?.equals(data?.clubid) != true) {
                lifecycleScope.executeAsyncTask(onPreExecute = {
                }, doInBackground = {
                    try {
                        File(DOWNLOAD_DIR_PATH + "/OVERLAY/").deleteRecursively()
                    } catch (e: Exception) {
                        println("Error during unzip: ${e.message}")
                    }
                    "Result" // send data to "onPostExecute"
                }, onPostExecute = {
                    downLoadFile(currentItem)
                })
            } else if (!savedData.tilesversion.equals(data?.tilesversion)) {
                downLoadFile(currentItem)
            } else if (!requireContext().isFileDownloaded(
                    currentItem,
                    DOWNLOAD_DIR_PATH + "/OVERLAY/" + Utils.getFileName(currentItem.getUri())
                )
            ) {
                downLoadFile(currentItem)
            } else {
                binding.txtProgress.text = "${HUNDRED}%"
                binding.txtRemainingTime.text = getString(R.string.download_completed)
                val path = DOWNLOAD_DIR_PATH + "/OVERLAY/" + Utils.getFileName(currentItem.getUri())
                if (!prefs.getZipExtracted()) {
                    unzipFile(path, File(path).parent)
                } else {
                    callMediaInfoData()
                }
            }
        } else {
            toastMessage("Please go back and enter valid code")
        }
        saveJSONFile(Gson().toJson(data), TILE_JSON_PATH)
        data?.let { prefs.saveTilesData(it) }
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

    private fun downLoadFile(item: FileItem) {
        HyperLog.d(TAG, "Download Start ${item.uri}")
        downloader = getDownloader(item, item.listener)
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
                prefs.setZipExtracted(false)
                deleteAndStartDownload(downloader, item.token)
            } else {
                //int status = Downloader.pause(this, item.getToken());
                prefs.setZipExtracted(false)
                deleteAndStartDownload(downloader, item.token)
            }
        } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.SUCCESSFUL) {
            prefs.setZipExtracted(false)
            deleteAndStartDownload(downloader, item.token)
            DeviceLogs.e("download", "download successfull")
        } else {
            prefs.setZipExtracted(false)
            deleteAndStartDownload(downloader, item.token)
        }
        if (item.listener == null) {
            item.listener = getDownloadListener(
                item,
                binding.txtProgress,
                binding.txtRemainingTime,
                binding.txtPauseReason
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
        tvPercent: AppCompatTextView,
        txtRemainingTime: AppCompatTextView,
        txtPauseReason: AppCompatTextView
    ): DownloadListener {
        return object : DownloadListener {
            var lastStatus = DownloadStatus.NONE
            var lastPercent = 0
            override fun onComplete(
                totalBytes: Int,
                mDownloadItem: DownloadItem?,
                downloadUri: String?
            ) {
                HyperLog.d(TAG, "Download Complete ${item.uri}")
                item.downloadStatus = DownloadStatus.SUCCESSFUL
                tvPercent.setText("${HUNDRED}%")
                txtRemainingTime.text = getString(R.string.download_completed)
                txtRemainingTime.text = getString(R.string.un_zip_file)
                if (lastStatus !== DownloadStatus.SUCCESSFUL) {
                    if (downloadUri.isNullOrEmpty()) {
                        mDownloadItem?.filePath?.let {
                            txtRemainingTime.text = getString(R.string.un_zip_file)
                            unzipFile(
                                it,
                                File(mDownloadItem?.filePath).parent
                            )
                        }
                    } else {
                        txtRemainingTime.text = getString(R.string.un_zip_file)
                        Uri.parse(downloadUri).path?.let { unzipFile(it, File(it).parent) }
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
                    txtPauseReason.visibility = View.VISIBLE
                    txtPauseReason.text = getString(R.string.no_internet_waiting_for_network)
                } else {
                    txtPauseReason.visibility = View.GONE
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
                txtPauseReason.visibility = View.GONE
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
                txtPauseReason.visibility = View.GONE
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
                tvPercent.setText("0%")
                txtPauseReason.visibility = View.GONE
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
                txtPauseReason.visibility = View.GONE
                if (percent > lastPercent) {
                    lastPercent = percent
                    tvPercent.setText("$percent%")
                    txtRemainingTime.text = ""
                    binding.txtRemainingTime.text = ""
//                    var remainTime = ((totalBytes - downloadedBytes) / (downloadSpeed * 1000)).toLong()
//                    if ((remainTime >= 0) and (remainTime <= 86400)) {
//                        txtRemainingTime.text = "${getString(R.string.remaining)}: ${remainTime.formatTime()}"
//                    } else {
//                        binding.txtRemainingTime.text = getString(R.string.remaining_calculating)
//                    }
                }
                lastStatus = DownloadStatus.RUNNING
            }
        }
    }

    fun unzipFile(zipFilePath: String, outputDir: String) {
        zipExtraction = lifecycleScope.executeAsyncTask(onPreExecute = {
            HyperLog.d(TAG, "Unzip File start ${zipFilePath}")
            binding.txtProgress.text = "${HUNDRED}%"
            binding.txtRemainingTime.text = getString(R.string.un_zip_file)
        }, doInBackground = { scope ->
            val ram = context.getUsedMemorySize()
            try {
                HyperLog.ze(
                    "Unzip",
                    "Unzip Start Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB"
                )
                UnZipUtil().unzip(zipFilePath, outputDir) {
                    requireActivity().runOnUiThread {
                        if (scope.isActive)
                            binding.txtRemainingTime.text =
                                "${getString(R.string.un_zip_file)} $it%"
                    }
                }
                HyperLog.ze(
                    "Unzip",
                    "Unzip completed Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB"
                )
                println("Unzip completed successfully.")
            } catch (e: Exception) {
                HyperLog.ze(
                    "Unzip",
                    "Unzip error Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB"
                )
                DeviceLogs.e("unzip", "Error during unzip")
                println("Error during unzip: ${e.message}")
                return@executeAsyncTask "unzip Error"
            }
            "Result" // send data to "onPostExecute"
        }, onPostExecute = {
            HyperLog.d(TAG, "Unzip File complete ${zipFilePath}")
            if (it.equals("unzip Error", ignoreCase = true)) {
                DeviceLogs.e("unzip", "Error during unzip1")
                try {
                    File(zipFilePath).deleteRecursively()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.txtProgress.text = ""
                callDownloadInfoAPI()
            } else {
                prefs.setZipExtracted(true)
                DeviceLogs.e("unzip", "Error during unzip2")
                binding.txtProgress.text = "${HUNDRED}%"
                binding.txtRemainingTime.text = getString(R.string.download_completed)
                callMediaInfoData()
            }
        })
    }

    private fun callMediaInfoData() {
//        if (!prefs.getIsWifiOn() and viewModel.wifiManager.isWifiEnabledValue){
//            viewModel.wifiManager.closeWiFi(requireActivity())
//        }
        loadingDialog(false)
        findNavController().navigate(R.id.action_download_ads, arguments ?: Bundle.EMPTY)
//        if (arguments?.containsKey("code") == true) {
//            if (!arguments?.getString("code").isNullOrEmpty()) {
//                arguments?.getString("code")?.let { it1 ->
//                    loadingDialog(true)
//                    viewModel.getMediaData(it1)
//                }
//            } else {
//                installOrLaunch()
//            }
//        } else {
//            installOrLaunch()
//        }
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
                Storage.DIRECTORY_DOWNLOADS, "/OVERLAY/" + Utils.getFileName(item.getUri())
            )
            .setNotificationTitle("Downloading")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true) //Api 16 and higher
        }
        return request
    }

    private fun installOrLaunch() {
        if (LegacyUtils.isDeviceOwner(requireContext())) {
            prefs.setIsKiosk(true)
            LegacyUtils.lockDownApp(requireContext(), true, whitelistPackage = izonPackageName)
        } else {
            toastMessage("Please make this application device owner")
        }
        binding.txtRemainingTime.text = getString(R.string.download_completed)
        binding.txtProgress.setText("${HUNDRED}%")
        if (requireContext().isPackageInstalled(izonPackageName)) {
            prefs.setIsGolfAppLaunch(true)
            val launchIntent =
                requireContext().packageManager.getLaunchIntentForPackage(izonPackageName)
            launchIntent?.let { startActivity(it) }
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                "OVERLAY/IzonGolf-012.apk"
            )
            if (file.exists()) {
                installApplication(requireContext(), file, izonPackageName, "")
            } else {
                toastMessage("IzonGolf application not found")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (downloader?.getStatus(currentItem.getToken()) === DownloadStatus.SUCCESSFUL) {
//                if (currentItem.mediaItem == null) {
//                    downloader?.deleteFile(currentItem.getToken(), null)
//                }
            } else {
                downloader?.pause()
            }
            clearAllDownloadThread()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupUI() {
        when (prefs.getManualTheme()) {
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

    private fun handleGetMediaInfo(state: Resource<MediaDownloadResponse>) {
        mediaTotalBytes = 0
        mediaDownloadedBytes = 0
        when (state) {
            is Resource.Success -> {
                loadingDialog(false)
                isRefresed = true
                state.data?.forEach {
                    mediaTotalBytes += it.size ?: 0
                }
                binding.txtRemainingTime.text = ""
                retryCount = 0
                startMediaDownload(state.data, state.data?.size)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    getToken(viewModel.viewModelScope, viewModel.repository, onSuccess = {
//                        loadingDialog(false)
                        callMediaInfoData()
                    }, onFail = {
                        loadingDialog(false)
                        installOrLaunch()
                    })
                } else {
                    loadingDialog(false)
                    toastMessage(state.message)
                    installOrLaunch()
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

    private fun startMediaDownload(data: MediaDownloadResponse?, size: Int?) {
        if (isRefresed) {
            if (data.isNullOrEmpty()) {
                installOrLaunch()
            } else {
                binding.txtMessage.text = getString(R.string.downloading_ads_content)
                HyperLog.d(TAG, "Download Media Start ${data.get(0).url}")
                downMediaLoadFile(data.get(0), data, size)
            }
        } else {
            data?.clear()
        }
    }

    private fun getMediaDownloadListener(
        item: FileItem,
        data: MediaDownloadResponse?,
        size: Int?,
        tvPercent: AppCompatTextView,
        txtRemainingTime: AppCompatTextView,
        txtPauseReason: AppCompatTextView
    ): DownloadListener {
        var lastStatus = DownloadStatus.NONE
        var lastPercent = 0
        return object : DownloadListener {
            override fun onComplete(
                totalBytes: Int,
                mDownloadItem: DownloadItem?,
                downloadUri: String?
            ) {
                item.downloadStatus = DownloadStatus.SUCCESSFUL
//                txtRemainingTime.text = getString(R.string.download_completed)
//                binding.txtRemainingTime.text = getString(R.string.remaining_calculating)
                binding.txtRemainingTime.text = ""
                if (lastStatus !== DownloadStatus.SUCCESSFUL) {
                    item.listener = null
                    data?.remove(item.mediaItem)
                    tvPercent.setText("${getMediaDownloadPer(size, data?.size, 0)}%")
                    startMediaDownload(data, size)
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
                    txtPauseReason.visibility = View.VISIBLE
                    txtPauseReason.text = getString(R.string.no_internet_waiting_for_network)
                } else {
                    txtPauseReason.visibility = View.GONE
//                    downloader?.resume()
                }
                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
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
                txtPauseReason.visibility = View.GONE
                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
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
                    startMediaDownload(data, size)
                }
                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
                txtPauseReason.visibility = View.GONE
                lastStatus = DownloadStatus.FAILED
            }

            override fun onCancel(
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.CANCELED) {
                    item.downloadStatus = DownloadStatus.CANCELED
                    try {
                        startMediaDownload(data, size)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                tvPercent.setText("${getMediaDownloadPer(size, data?.size, 0)}%")
                txtPauseReason.visibility = View.GONE
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
                txtPauseReason.visibility = View.GONE
                if (percent > lastPercent) {
                    lastPercent = percent
                    tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
                    txtRemainingTime.text = ""
                    binding.txtRemainingTime.text = ""
//                    var remainTime = ((totalBytes - downloadedBytes) / (downloadSpeed * 1000)).toLong()
//                    if ((remainTime >= 0) and (remainTime <= 86400)) {
//                        txtRemainingTime.text = "${getString(R.string.remaining)}: ${remainTime.formatTime()}"
//                    } else {
////                    binding.txtRemainingTime.text = getString(R.string.remaining_calculating)
//                    }
                }
                lastStatus = DownloadStatus.RUNNING
            }
        }
    }

    private fun getMediaDownloadPer(totalSize: Int?, remainSize: Int?, currentPer: Int?): String {
        var per = "0"
        val perItem = 100f / (totalSize?.toFloat() ?: 1f)
        per =
            ((((totalSize ?: 0f).toFloat() - (remainSize ?: 0).toFloat()) * perItem) + ((currentPer
                ?: 0) / (totalSize
                ?: 1))).toInt().toString()
        return per
    }

    private fun downMediaLoadFile(
        mediaItem: MediaDownloadResponseItem,
        data: MediaDownloadResponse?,
        size: Int?
    ) {
        val item = FileItem()
        item.mediaItem = mediaItem
        item.uri = mediaItem.url
        item.fileName = mediaItem.filename
        item.token = mediaItem.filename
        item.fileSize = mediaItem.size ?: 0
        item.networkType = mediaItem.networktype
        item.listener = getMediaDownloadListener(
            item,
            data,
            size,
            binding.txtProgress,
            binding.txtRemainingTime,
            binding.txtPauseReason
        )
        currentItem = item
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
                            startMediaDownload(data, size)
                        }, {
                            retryCount = 3
                            loadingDialog(false);
                            startMediaDownload(data, size)
                        })
                    } else {
                        retryCount = 3
                        loadingDialog(false);
                        startMediaDownload(data, size)
                    }
                },
                {
                    loadingDialog(false);
                    startMediaDownload(data, size)
                })
            retryCount++
        }*/
        /*else if (mediaItem?.networktype?.isDownloadSkip(requireContext()) == true) {
            item.listener = null
            data?.remove(item.mediaItem)
            binding.txtProgress.setText("${getMediaDownloadPer(size, data?.size, 0)}%")
            DeviceLogs.e(
                "complete media Download",
                "$size remain ${data?.size} ${item.fileName}"
            )
            startMediaDownload(data, size)
        } else */
        if (!requireContext().isFileDownloaded(
                currentItem,
                DOWNLOAD_DIR_PATH + "/CONTENT/" + if (item.mediaItem != null) item.mediaItem?.filename else Utils.getFileName(
                    item.getUri()
                )
            )
        ) {
            //Showing progress for running downloads.
            showMediaProgress(item, item.listener)
            downloader = getMediaDownloader(item, item.listener)
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
                    deleteAndStartDownload(downloader, item.token)
                } else {
                    //int status = Downloader.pause(this, item.getToken());
                    deleteAndStartDownload(downloader, item.token)
                }
            } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.SUCCESSFUL) {
                try {
                    deleteAndStartDownload(downloader, item.token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                DeviceLogs.e("download", "download successfull")
            } else {
                deleteAndStartDownload(downloader, item.token)
            }
        } else {
            item.listener = null
            data?.remove(item.mediaItem)
            binding.txtProgress.setText("${getMediaDownloadPer(size, data?.size, 0)}%")
            DeviceLogs.e(
                "complete media Download",
                "$size remain ${data?.size} ${item.fileName}"
            )
            startMediaDownload(data, size)
        }
    }

    private fun deleteAndStartDownload(downloader: Downloader?, token: String?) {
        val ram = context.getUsedMemorySize()
        HyperLog.ze(
            "Unzip",
            "Download Start Facility code ${prefs.getFacilityCode()} Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB"
        )
        downloader?.deleteFile(token, object : ActionListener {
            override fun onSuccess() {
                downloader?.start()
            }

            override fun onFailure(error: Errors?) {
                downloader?.start()
            }

        })
    }

    private fun showMediaProgress(item: FileItem, listener: DownloadListener?) {
        getMediaDownloader(item, listener)?.showProgress()
    }

    private fun getMediaDownloader(item: FileItem, listener: DownloadListener?): Downloader? {
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
                Storage.DIRECTORY_DOWNLOADS,
                "/CONTENT/" + if (item.mediaItem != null) item.mediaItem?.filename else Utils.getFileName(
                    item.getUri()
                )
            )
            .setNotificationTitle("Downloading")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true) //Api 16 and higher
        }
        return request
    }

    override fun onResume() {
        super.onResume()
        exitKioskMode()
    }

    private fun exitKioskMode() {
        HyperLog.d(TAG, "Exit Kiosk Mode Dialog Display")
        if (requireActivity() is HostActivity) {
            val hostActivity = requireActivity() as HostActivity
            if ((hostActivity.mDialog?.isShowing != true) and (prefs.getIsKiosk()) and (prefs.getIsGolfAppLaunch()) and (isPauseFirstTime)) {
                hostActivity.exitKioskMode(onCancel = { dialog, pin, view ->
                    installOrLaunch()
                })
            }
        } else if ((mDialog?.isShowing != true) and (prefs.getIsKiosk()) and (prefs.getIsGolfAppLaunch()) and (isPauseFirstTime)) {
            mDialog = requireContext().simpleInputDialog(
                getString(R.string.enter_a_pin_to_exit_kiosk_mode), onOK = { dialog, pin, view ->
                    view?.hideSoftKeyboard()
                    if (pin.isEmpty()) {
                        toastMessage(getString(R.string.please_enter_pin))
                    } else if (!viewModel.network.isConnected()) {
                        if (pin.equals(prefs.getExitKioskCode())) {
                            HyperLog.d(TAG, "Exit Kiosk Mode")
                            dialog.dismiss()
//                                finish()
                            findNavController().navigate(R.id.action_exit_kiosk)
                        }
                    } else {
                        val izonAppVersion = try {
                            requireContext().packageManager?.getPackageInfo(
                                izonPackageName,
                                0
                            )?.versionName
                                ?: "0.0.0"
                        } catch (e: PackageManager.NameNotFoundException) {
                            "0.0.0"
                        }
                        val exitKioskRequest = ExitKioskRequest(
                            deviceid = requireContext().getAndroidId(),
                            deviceiccid = requireContext().getIccId().first,
                            clubcode = prefs.getFacilityCode(),
                            dcappversion = BuildConfig.VERSION_NAME,
                            izongolfappversion = izonAppVersion,
                            exitcode = pin,
                            clientdatetime = todayDate(withFormat = API_RETURN_UPDATE_FORMAT)
                        )

                        validateKioskCodeV2(
                            viewModel.viewModelScope,
                            viewModel.repository,
                            exitKioskRequest,
                            onSuccess = {
//                                LegacyUtils.lockDownApp(
//                                    requireContext(),
//                                    false,
//                                    finishActivity = false
//                                )
                                dialog.dismiss()
//                                requireActivity().finish()
                                findNavController().navigate(R.id.action_exit_kiosk)
                            },
                            onFail = {
                                toastMessage(getString(R.string.please_enter_correct_pin))
                                requireContext().simpleAlertOverLay(it.toString())
                            })
                    }
                }, onCancel = { dialog, pin, view ->
                    installOrLaunch()
                })
            isPauseFirstTime = false
        }
    }

    override fun onPause() {
        super.onPause()
        isPauseFirstTime = true
    }
}









