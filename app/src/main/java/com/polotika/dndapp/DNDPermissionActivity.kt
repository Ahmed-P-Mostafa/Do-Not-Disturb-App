package com.polotika.dndapp

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.polotika.dndapp.databinding.ActivityDndpermissionBinding

class DNDPermissionActivity : AppCompatActivity() {
    lateinit var binding : ActivityDndpermissionBinding
    val manager :NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    val requestResult =   registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (manager.isNotificationPolicyAccessGranted) {

            binding.nextBtn.isEnabled = true
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_dndpermission)
        binding.askPermissionBtn.setOnClickListener {

            requestNotificationPolicyRequest()

        }
        binding.nextBtn.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
    private fun requestNotificationPolicyRequest() {

        if (!manager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            requestResult.launch(intent)

        } else {
            startActivity(Intent(this,MainActivity::class.java))

        }
    }
}