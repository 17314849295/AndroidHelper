package com.example.android.helper.library

import androidx.multidex.MultiDexApplication

open class HelperApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        cNetworkStatesManager = NetworkStatesManager(this)
    }

    companion object {
        private var cNetworkStatesManager: NetworkStatesManager? = null
        val networkStatesManager get() = cNetworkStatesManager!!
    }
}