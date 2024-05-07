package com.izontechnology.dcapp.presentation.exit_kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.common.getFileName
import com.izontechnology.dcapp.data.common.toRequestBody
import com.izontechnology.dcapp.data.common.toRequestBodyFile
import com.izontechnology.dcapp.databinding.FragmentExitKioskBinding
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import com.izontechnology.dcapp.utils.API_RETURN_DATE_FORMAT
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.FACILITY_JSON_PATH
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.RealPathUtil
import com.izontechnology.dcapp.utils.TILE_JSON_PATH
import com.izontechnology.dcapp.utils.confirmationDialog
import com.izontechnology.dcapp.utils.executeAsyncTask
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getImei
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.scheduleWork
import com.izontechnology.dcapp.utils.sendWifiToggle
import com.izontechnology.dcapp.utils.simpleAlert
import com.izontechnology.dcapp.utils.todayDate
import com.izontechnology.dcapp.utils.uploadLogFile
import com.izontechnology.dcapp.utils.wifi.WiFiManager
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import java.io.File
import java.io.PrintWriter
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class ExitKioskFragment : BaseFragment<FragmentExitKioskBinding, ExitKioskFragmentVm>() {

    @Inject
    lateinit var prefs: SharedPrefs

    @Inject
    lateinit var wiFiManager: WiFiManager

    var resultLauncher: ActivityResultLauncher<Intent>? = null
    var uploadFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    loadingDialog(true)
                    val data: Intent? = result.data
                    var filePath = RealPathUtil.getRealPath(requireContext(), data?.data)
                    uploadFile = File(filePath.toString())
//                    var filePath = FileProvider.getUriForFile(requireContext(),
//                        BuildConfig.APPLICATION_ID.toString() + ".provider",File(data?.data.toString()))
                    uploadLogFile(viewModelScope = viewModel.viewModelScope,
                        repository = viewModel.repository,
                        deviceid = requireContext().getAndroidId()?.toRequestBody(),
                        deviceimei = requireContext().getImei()?.toRequestBody(),
                        clientTs = todayDate(withFormat = API_RETURN_DATE_FORMAT).toRequestBody(),
                        filename = data?.data?.getFileName(requireContext()).toRequestBody(),
                        code = prefs.getFacilityCode().toRequestBody(),
                        fileUpload = uploadFile?.toRequestBodyFile("fileUpload"),
                        onResponse = { viewModel.mediaResponse.value = it })
                }
            }
    }

    override fun observeViewModel() {
        viewModel.mediaResponse.observe(this, {
            handleLogUpload(it)
        })
    }

    private fun handleLogUpload(state: Resource<ResponseBody>) {
        when (state) {
            is Resource.Success -> {
                loadingDialog(false)
                try {
                    if (!(uploadFile?.path?.contains("extractionlog",true) == true)){
//                        uploadFile?.delete()
                    val writer: PrintWriter = PrintWriter(uploadFile)
                    writer.print("")
                    writer.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                var dialog =
                    requireContext().simpleAlert(getString(R.string.file_uploaded_successfully),
                        onDismiss = {
                            if (requireActivity() is HostActivity) {
                                (requireActivity() as HostActivity).isBackAllow = true
                            }
//                            requireActivity().onBackPressed()
                        })
                Handler(Looper.getMainLooper()).postDelayed({ dialog?.dismiss() }, 3000)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                loadingDialog(false)
                toastMessage(state.message)
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

    private fun tiemPicker() {
        // Get Current Time
        val c: Calendar = Calendar.getInstance()
        var mHour = c.get(Calendar.HOUR_OF_DAY)
        var mMinute = c.get(Calendar.MINUTE)

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            requireContext(), OnTimeSetListener { view, hourOfDay, minute ->
                mHour = hourOfDay
                mMinute = minute
                prefs.setNightUpdateHour(mHour)
                prefs.setNightUpdateMinute(mMinute)
                scheduleWork(
                    if (prefs.getNightUpdateHour() == -1) 1 else prefs.getNightUpdateHour(),
                    if (prefs.getNightUpdateMinute() == -1) 1 else prefs.getNightUpdateMinute()
                )

            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

    override fun initViewBinding() {
        binding.apply {
            if (BuildConfig.FLAVOR.equals("dev")) {
                btnTime.visibility = View.VISIBLE
            }
            btnUploadLog?.setOnClickListener {
                val root = File(DOWNLOAD_DIR_PATH + "/LOGS/")
                var uri = Uri.fromFile(File(DOWNLOAD_DIR_PATH))
                if (root.exists()) {
                    uri = Uri.fromFile(root)
                }
                val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
//                chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
//                chooseFile.setDataAndType(uri, "text/plain")
                chooseFile.setType("text/plain")
                chooseFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                chooseFile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                chooseFile.data = uri
                resultLauncher?.launch(chooseFile)

            }
            btnClose?.setOnClickListener {
                try {
                    requireContext().cacheDir.deleteRecursively()
                    LegacyUtils.lockDownApp(requireContext(), false, finishActivity = false)
                    val am =
                        requireContext().getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager?
                    am?.killBackgroundProcesses(izonPackageName)
                    am?.killBackgroundProcesses(requireContext().packageName)
                    requireActivity().finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            btnRestart?.setOnClickListener {
                restartDCApp()
            }
            btnReset?.setOnClickListener {
                requireContext().confirmationDialog(getString(R.string.do_you_really_want_to_erase_all_course_data),
                    btnPositiveClick = {
                        clearAllDataAndRestart()
                    })
            }
            btnTime.setOnClickListener {
                tiemPicker()
            }
            swWifi.isChecked = wiFiManager.isWifiEnabledValue
            prefs.setIsWifiOn(swWifi.isChecked)
            swWifi.setOnCheckedChangeListener { buttonView, isChecked ->
                prefs.setIsWifiOn(isChecked)
                if (isChecked) {
                    wiFiManager.openWiFi(requireActivity())
                } else {
                    wiFiManager.closeWiFi(requireActivity())
                }
                callWifiToggleAPI(isChecked)
            }
        }
    }

    private fun callWifiToggleAPI(checked: Boolean) {
        sendWifiToggle(
            viewModelScope = viewModel.viewModelScope,
            repository = viewModel.repository,
            isWifi = checked
        )
    }

    private fun clearAllDataAndRestart() {
        prefs.clearAll()
        lifecycleScope.executeAsyncTask(onPreExecute = {
            loadingDialog(true)
        }, doInBackground = {
            Downloader.getDownloadsList(requireContext()).forEach {
                Downloader.getInstance(requireContext()).deleteFile(it.token, null)
            }
            try {
                File(DOWNLOAD_DIR_PATH + "/OVERLAY/").deleteRecursively()
            } catch (e: Exception) {
                println("Error during unzip: ${e.message}")
            }
            try {
                File(DOWNLOAD_DIR_PATH + "/CONTENT/").deleteRecursively()
            } catch (e: Exception) {
                println("Error during unzip: ${e.message}")
            }
            try {
                File(FACILITY_JSON_PATH).deleteRecursively()
            } catch (e: Exception) {
                println("Error during unzip: ${e.message}")
            }
            try {
                File(TILE_JSON_PATH).deleteRecursively()
            } catch (e: Exception) {
                println("Error during unzip: ${e.message}")
            }
            "Result" // send data to "onPostExecute"
        }, onPostExecute = {
            loadingDialog(false)
            restartDCApp()
        })
    }

    private fun restartDCApp() {
        prefs.setManualTheme(1)
        try {
            val homeIntent = Intent(context, HostActivity::class.java)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context?.startActivity(homeIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}