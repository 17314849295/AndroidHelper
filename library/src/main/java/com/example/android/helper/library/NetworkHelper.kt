package com.example.android.helper.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

fun FragmentActivity.listenNetworkState(listener: NetworkStatesManager.NetworkStatesListener) {
    @Suppress("unused")
    NetworkHelper(this, listener)
}

class NetworkHelper(
    activity: FragmentActivity,
    private val listener: NetworkStatesManager.NetworkStatesListener
) : LifecycleObserver {

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    @Suppress("unused")
    private fun onCreate() {
        HelperApplication.networkStatesManager.register(listener)
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        HelperApplication.networkStatesManager.unregister(listener)
    }
}

class NetworkStatesManager(
    context: Context,
) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    onNetworkStateChanged(true)
                }

                override fun onAvailable(network: Network) {
                    onNetworkStateChanged(false)
                }
            })
        }
    }

    private val listeners = mutableListOf<NetworkStatesListener>()

    fun register(listener: NetworkStatesListener) {
        listeners += listener
    }

    fun unregister(listener: NetworkStatesListener) {
        listeners -= listener
    }

    fun onNetworkStateChanged(disconnected: Boolean) {
        Log.d(TAG, "disconnected = $disconnected")
        listeners.forEach {
            it.onNetworkStateChanged(disconnected)
        }
    }

    interface NetworkStatesListener {
        fun onNetworkStateChanged(disconnected: Boolean)
    }

    companion object {
        private const val TAG = "NetworkHelperTAG"
    }
}

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val networkConnected = context.isNetworkConnected()
        Log.d(TAG, "networkConnected = $networkConnected")
        HelperApplication.networkStatesManager.onNetworkStateChanged(!networkConnected)
    }

    companion object {
        private const val TAG = "NetworkHelperTAG"
    }
}

@Suppress("DEPRECATION")
@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
        return activeNetworkInfo.isConnected
    }
}