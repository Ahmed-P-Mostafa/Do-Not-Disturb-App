package com.polotika.dndapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.polotika.dndapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.*


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
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
            calendar.set(Calendar.MINUTE, binding.timePicker.minute)
            val millis = calendar.timeInMillis
            viewModel.onStartButtonClicked(millis)
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.isMyServiceRunning(DNDService::class.java)
        viewModel.isNotificationPoliceGranted()
    }

    private fun observers() {
        viewModel.startButtonTextEvent.observe(this@MainActivity) {
            binding.startButton.text = it
        }

        lifecycleScope.launchWhenResumed {
            viewModel.navigationEvent.collect {
                when (it) {
                    is MainActivityNavigationEvent.NavigateMainActivityToPermissionsActivity -> {
                        startActivity(Intent(this@MainActivity, DNDPermissionActivity::class.java))
                        finish()
                    }
                    is MainActivityNavigationEvent.NavigateMainActivityToDndService -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(it.intent)
                        } else {
                            startService(it.intent)
                        }
                    }
                    is MainActivityNavigationEvent.NavigateMainActivityToAutoStartPermission -> {
                        Log.d(TAG, "observers: ${it.intent.data}")
                        showAutoStartAlertDialog(it.intent)

                    }
                }
            }
        }
    }

    private fun showAutoStartAlertDialog(intent: Intent) {
        AlertDialog.Builder(this).setTitle(getString(R.string.auto_start_dialog_title))
            .setMessage("${Build.MANUFACTURER} " + getString(R.string.auto_start_dialog_message))
            .setPositiveButton(getString(R.string.auto_start_dialog_ok)) { d, _ ->
                viewModel.setAutoStartEnabled()
                startActivity(intent)
            }.setNegativeButton(getString(R.string.auto_start_dialog_cancel)) { d, _ ->
                d.dismiss()
            }.show()
    }

}