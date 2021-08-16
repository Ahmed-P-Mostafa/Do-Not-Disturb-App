package com.polotika.dndapp

import android.widget.TimePicker
import androidx.databinding.BindingAdapter

class BindingAdapter {

    companion object{
        @BindingAdapter("android:setHour")
        @JvmStatic
        fun setHour(picker:TimePicker,hour:Int){
            picker.hour = hour
        }

        @BindingAdapter("android:setMinute")
        @JvmStatic
        fun setMinute(picker:TimePicker,minute:Int){
            picker.minute = minute
        }
    }

}