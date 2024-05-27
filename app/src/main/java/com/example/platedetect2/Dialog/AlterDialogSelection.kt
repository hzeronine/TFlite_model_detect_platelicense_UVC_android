package com.example.platedetect2.Dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.platedetect2.DevicesFragment.ListItem
import java.util.Locale

class AlterDialogSelection(
    private val ArrayDevice: ArrayList<ListItem>,
    private val requestCode: Int
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val alterBuilder = AlertDialog.Builder(it)
            var checkedIndex = -1
            var StringDevices = emptyArray<String>()
            Toast.makeText(context,"Device Found: " + ArrayDevice.size,Toast.LENGTH_SHORT).show()
            for(value in ArrayDevice){

                val deviceName = value.device.productName
                var x : String = ""
                if (value.driver == null)
                    x = if (deviceName == null) "Unknown Device" else "$deviceName, <no driver>"
                else if (value.driver.ports.size == 1)
                    x = value.device.getProductName() + ", driver: " + value.driver.javaClass.getSimpleName()
                            .replace("SerialDriver", "")
                else
                    x = value.device.getProductName() + ", driver: " + value.driver.javaClass.getSimpleName()
                        .replace("SerialDriver", "") + ", Port " + value.port

                x += "\n"+String.format(
                    Locale.US,
                    "Vendor %04X, Product %04X",
                    value.device.getVendorId(),
                    value.device.getProductId()
                )

                StringDevices+= (x)
            }

            alterBuilder.setTitle("Select a Device")
            alterBuilder.setSingleChoiceItems(StringDevices, -1, DialogInterface.OnClickListener { dialog, which ->
                checkedIndex = which
            })
            alterBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                var intent : Intent = Intent()
                intent.putExtra("position",which)
                requireParentFragment().onActivityResult(requestCode,checkedIndex, intent)
            })

            alterBuilder.create()
        } ?: throw IllegalStateException("Exception !! Activity is null")
    }
}

