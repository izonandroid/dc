package com.izontechnology.dcapp.presentation.splash_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseActivity
import com.izontechnology.dcapp.databinding.ActivitySplashBinding
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashActivityVM>() {
    @Inject
    lateinit var pref: SharedPrefs

    override fun observeViewModel() {

    }


    override fun initViewBinding() {
//        if (LegacyUtils.isDeviceOwner(this)) {
//            pref.setIsKiosk(true)
//            LegacyUtils.lockDownApp(this, true, whitelistPackage = com.izontechnology.dcapp.utils.izonPackageName)
//        } else {
//            toastMessage("Please make this application device owner")
//        }
        pref.setManualTheme(1)
        try {
            if (LegacyUtils.isDeviceOwner(this)) {
                pref.setIsKiosk(true)
                LegacyUtils.lockDownApp(
                    this,
                    true,
                    whitelistPackage = com.izontechnology.dcapp.utils.izonPackageName
                )
            } else {
                toastMessage("Please make this application device owner")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        pref.saveToken("bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVpZCI6IjgyMDJkNDI5LWQwY2QtNGMyOS1iZTM0LTA0Y2VkYzQyYTdhMyIsInVzZXIiOiJkY2FwcCIsImV4cCI6MTcwOTAxMzg4MiwiaXNzIjoiSVpPTiIsImF1ZCI6IkRjQVBQIn0.Pu3QLFa5Ug4ivQHe9kVJwGwoAaBfKgAiincLAkxNKh4")
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this,
                    HostActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtras(Bundle.EMPTY)
            )
        }, 2000)
    }

}