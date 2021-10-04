package com.polotika.dndapp

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(@ApplicationContext private val applicationContext: Context,
                                        private val prefs:PreferencesServices) :ViewModel() {

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
            viewModelScope.launch {
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
            sendStartServiceIntent(value)
        }else{
            sendStopServiceIntent()
        }

    }

    private fun sendStopServiceIntent() {
        startButtonTextEvent.value = applicationContext.getString(R.string.start)
        val stopIntent = Intent(applicationContext, DNDService::class.java)
        stopIntent.action = stop
        viewModelScope.launch {
            prefs.setTime(-1L)
            navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToDndService(stopIntent))

        }
    }

    private fun sendStartServiceIntent(value: Long){
        startButtonTextEvent.value = applicationContext.getString(R.string.stop)

        val startIntent = Intent(applicationContext, DNDService::class.java)
        startIntent.action = start
        val calendar = Calendar.getInstance()

        var millis = value - calendar.timeInMillis
        if (millis<0){
            millis += AlarmManager.INTERVAL_DAY
        }

        viewModelScope.launch {
            prefs.setTime(millis)
            navigateEventChannel.send(MainActivityNavigationEvent.NavigateMainActivityToDndService(startIntent))
        }
    }

}

sealed class MainActivityNavigationEvent(){
    data class NavigateMainActivityToDndService(val intent: Intent):MainActivityNavigationEvent()
    object NavigateMainActivityToPermissionsActivity:MainActivityNavigationEvent()
}