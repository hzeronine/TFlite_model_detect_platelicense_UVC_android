package com.example.platedetect2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .add(R.id.fragment, DevicesFragment(), "devices").commit() else onBackStackChanged()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayShowHomeEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true

    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == intent!!.action) {
            val terminal: ArduinoConnectionActivity? =
                supportFragmentManager.findFragmentByTag("terminal") as ArduinoConnectionActivity?
            if (terminal != null) terminal.status("USB device detected")
        }
        super.onNewIntent(intent)
    }
}