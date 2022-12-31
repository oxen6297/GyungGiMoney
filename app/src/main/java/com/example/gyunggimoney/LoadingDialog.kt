package com.example.gyunggimoney

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle

class LoadingDialog(context: Context) : Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_layout)

        setCancelable(false)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}