package com.example.cinelog.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cinelog.R
import com.example.cinelog.databinding.LayoutCustomSnackbarBinding
import com.google.android.material.snackbar.Snackbar

fun View.showSuccessSnackbar(title: String, message: String) {
    val rootView = (this.context as? android.app.Activity)?.findViewById<View>(android.R.id.content) ?: this
    
    val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
    val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    
    snackbarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    snackbarLayout.setPadding(0, 0, 0, 0)

    val binding = LayoutCustomSnackbarBinding.inflate(LayoutInflater.from(context))
    binding.tvSnackbarTitle.text = title
    binding.tvSnackbarMessage.text = message
    binding.btnSnackbarAction.text = context.getString(R.string.label_ok)
    binding.btnSnackbarAction.setOnClickListener {
        snackbar.dismiss()
    }

    snackbarLayout.addView(binding.root, 0)
    snackbar.show()
}

fun View.showErrorSnackbar(title: String, message: String) {
    val rootView = (this.context as? android.app.Activity)?.findViewById<View>(android.R.id.content) ?: this
    
    val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
    val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    
    snackbarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    snackbarLayout.setPadding(0, 0, 0, 0)

    val binding = LayoutCustomSnackbarBinding.inflate(LayoutInflater.from(context))
    binding.tvSnackbarTitle.text = title
    binding.tvSnackbarMessage.text = message
    binding.tvSnackbarTitle.setTextColor(android.graphics.Color.parseColor("#FF5252")) // Red for error
    binding.ivSnackbarIcon.setImageResource(android.R.drawable.ic_dialog_alert)
    binding.ivSnackbarIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF5252"))
    binding.btnSnackbarAction.text = context.getString(R.string.label_ok)
    
    binding.btnSnackbarAction.setOnClickListener {
        snackbar.dismiss()
    }

    snackbarLayout.addView(binding.root, 0)
    snackbar.show()
}
