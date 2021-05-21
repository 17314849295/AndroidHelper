package com.example.android.helper

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.helper.library.NetworkStatesManager
import com.example.android.helper.library.listenNetworkState

class MainActivity : AppCompatActivity(), NetworkStatesManager.NetworkStatesListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listenNetworkState(this)
    }

    override fun onNetworkStateChanged(disconnected: Boolean) {
        Log.d(TAG, "disconnected = $disconnected")
    }

    companion object {
        private const val TAG = "MainActivityTAG"
    }
}
