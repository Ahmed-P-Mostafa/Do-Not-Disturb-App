package com.polotika.dndapp

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

class SharedViewModel(application: Application) :AndroidViewModel(application) {

   /* private val timer = object : CountDownTimer(30000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
        }

        override fun onFinish() {
            mTextField.setText("done!")
        }
    }.start()*/

}