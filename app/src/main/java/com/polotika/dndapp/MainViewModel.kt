package com.polotika.dndapp

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(@ApplicationContext private val applicationContext: Context,
                                        private val prefs:PreferencesServices,private val dispatcher:Dispatchers) :ViewModel() {
    init {
        viewModelScope.launch(dispatcher.IO) {
            if (!prefs.isAutoStartEnabled.first()){
                checkForManufacture()
            }
        }

    }
    private val TAG = "MainViewModel"

    val startButtonTextEvent = MutableLiveData(applicationContext.getString(R.string.start))
    private val stop = "STOP"
    private val start = "START"
    private val navigateEventChannel = Channel<MainActivityNavigationEvent>()
    private val manager: NotificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    val navigationEvent = navigateEventChannel.receiveAsFlow()



    fun isMyServiceRunning(serviceClass: Class<*>):Boolean {
        val manager = applicationContext.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                startButtonTextEvent.value = applicationContext.getString(R.string.stop)
                return true
            }
        }
        startButtonTextEvent.value = applicationContext.getString(R.string.start)
        return false
    }

    fun isNotificationPoliceGranted(){
        if (!manager.isNotificationPolicyAccessGranted){
            viewModelScope.launch(dispatcher.IO) {
                navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToPermissionsActivity)
            }
        }
    }

    // binding methods to set TimePicker time
    fun getHour(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)

    }

    fun getMinute(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MINUTE)
    }


    fun onStartButtonClicked(value:Long){
        if (!isMyServiceRunning(DNDService::class.java)){
            val calendar = Calendar.getInstance()
            var millis = value
            if (millis<calendar.timeInMillis){
                millis += AlarmManager.INTERVAL_DAY
            }

            sendStartServiceIntent(millis)
        }else{
            sendStopServiceIntent()
        }

    }

    private fun sendStopServiceIntent() {
        startButtonTextEvent.value = applicationContext.getString(R.string.start)
        val stopIntent = Intent(applicationContext, DNDService::class.java)
        stopIntent.action = stop
        viewModelScope.launch(dispatcher.IO){
            prefs.setTime(-1L)
            navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToDndService(stopIntent))

        }
    }

    private fun sendStartServiceIntent(value: Long){
        startButtonTextEvent.value = applicationContext.getString(R.string.stop)

        val startIntent = Intent(applicationContext, DNDService::class.java)
        startIntent.action = start


        viewModelScope.launch(dispatcher.IO) {
            prefs.setTime(value)
            navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToDndService(startIntent))
        }
    }

    fun checkForManufacture(){
        try {
            Log.d(TAG, "checkForDeviceManufacture: try")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val manufacturer = Build.MANUFACTURER
            Build.MANUFACTURER
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
            }else{
                intent.setData(Uri.parse("package:${applicationContext.packageName}"))

            }
            val list =
                applicationContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                Log.d(TAG, "checkForDeviceManufacture: list > 0")

            }
            Log.d(TAG, "checkForDeviceManufacture: ${intent.component?.packageName}")
            viewModelScope.launch(dispatcher.IO) {
                navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToAutoStartPermission(intent))
            }

        } catch (e: Exception) {
            Log.d(TAG, "checkForDeviceManufacture: catch ${e.localizedMessage}")
            Log.e(TAG, e.toString())
        }
    }

    fun setAutoStartEnabled() {
        viewModelScope.launch(dispatcher.IO) {
            prefs.setAutoStartEnabled()
        }

    }

}

sealed class MainActivityNavigationEvent(){
    data class NavigateMainActivityToDndService(val intent: Intent):MainActivityNavigationEvent()
    data class NavigateMainActivityToAutoStartPermission(val intent: Intent):MainActivityNavigationEvent()
    object NavigateMainActivityToPermissionsActivity:MainActivityNavigationEvent()
}