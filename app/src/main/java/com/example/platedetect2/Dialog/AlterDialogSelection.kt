package com.example.platedetect2.Dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.platedetect2.R
import com.example.platedetect2.utils.DeviceListControl

class AlterDialogSelection : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val alterBuilder = AlertDialog.Builder(it)
            var checkedIndex = -1
            var instance = DeviceListControl.getInstance()
            alterBuilder.setTitle("Select an option")
            alterBuilder.setSingleChoiceItems(R.array.read_modes, -1, DialogInterface.OnClickListener { dialog, which ->
                checkedIndex = which
            })
            alterBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                onActivityResult(1101,checkedIndex, Intent())
            })

            alterBuilder.create()
        } ?: throw IllegalStateException("Exception !! Activity is null")
    }
}