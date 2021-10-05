package com.polotika.dndapp

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.polotika.dndapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

/*
* TODO
*  1- set the timer in datastore and service read from datastore
*  2- try alarm manager and figure how to calc remaining time
*  3- give permission for auto start and notification permission for changing device mode
*  */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    lateinit var binding: ActivityMainBinding


    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        observers()

        binding.startButton.setOnClickListener {
          /*  val button = it as TextView
            when (button.text) {
                getString(R.string.start) -> {
                    binding.startButton.text = getString(R.string.stop)


                    val startIntent = Intent(this, DNDService::class.java)
                    startIntent.action = START
                    val calendar = Calendar.getInstance()
                    val calendar2 = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
                    calendar.set(Calendar.MINUTE, binding.timePicker.minute)
                    var millis = calendar.timeInMillis - calendar2.timeInMillis
                    if (millis<0){
                        millis += AlarmManager.INTERVAL_DAY
                    }

                    onStartButtonClicked(millis)
                    startIntent.putExtra("millis", millis)

                    startForegroundService(startIntent)
                }
                getString(R.string.stop) -> {
                    binding.startButton.text = getString(R.string.start)
                    val stopIntent = Intent(this, DNDService::class.java)
                    stopIntent.action = STOP
                    startService(stopIntent)
                }
            }*/
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
            calendar.set(Calendar.MINUTE, binding.timePicker.minute)
            val millis = calendar.timeInMillis
            viewModel.onStartButtonClicked(millis)
            compareTime(calendar.time)
        }
    }

    fun compareTime(long: Date){
        Log.d(TAG, "compareTime: nowTime = ${Calendar.getInstance().time}")
        Log.d(TAG, "compareTime: dialogTime $long")
    }

    override fun onResume() {
        super.onResume()
        viewModel.isMyServiceRunning(DNDService::class.java)
        viewModel.isNotificationPoliceGranted()
    }

    private fun observers(){
        viewModel.startButtonTextEvent.observe(this@MainActivity) {
            binding.startButton.text = it
        }

        lifecycleScope.launchWhenStarted {
            viewModel.navigationEvent.collect {
                when(it){
                    is MainActivityNavigationEvent.NavigateMainActivityToPermissionsActivity ->{
                        startActivity(Intent(this@MainActivity,DNDPermissionActivity::class.java))
                        finish()
                    }
                    is MainActivityNavigationEvent.NavigateMainActivityToDndService -> {
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                            startForegroundService(it.intent)
                        }else{
                            startService(it.intent)
                        }
                    }
                }
            }
        }


    }

    private fun checkForDeviceManufacture() {
        try {
            Log.d(TAG, "checkForDeviceManufacture: try")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val manufacturer = Build.MANUFACTURER
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            }
            val list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                Log.d(TAG, "checkForDeviceManufacture: list > 0")

            }
            Log.d(TAG, "checkForDeviceManufacture: ${intent.component?.packageName}")
            startActivity(intent)

        } catch (e: Exception) {
            Log.d(TAG, "checkForDeviceManufacture: catch ${e.localizedMessage}")
            Log.e(TAG, e.toString())
        }
    }
}