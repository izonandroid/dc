package com.izontechnology.dcapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import com.izontechnology.dcapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            context?.toast("izon device controll app boot completed")
            Log.e("ejfklggjlewk", "rcvd boot event, launching service");
            try{
                val homeIntent = Intent(context,HostActivity::class.java)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context?.startActivity(homeIntent)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}