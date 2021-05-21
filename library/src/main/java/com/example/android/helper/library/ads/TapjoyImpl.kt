package com.example.android.helper.library.ads

import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.android.helper.library.BuildConfig
import com.ironsource.mediationsdk.ISBannerSize
import com.tapjoy.*
import java.util.*

/**
 * 子类继承之后，需要处理activity生命周期，防止内存泄露
 */
open class TapjoyImpl(
    private val activity: AppCompatActivity,
    @StringRes private val resId: Int,
    private val userId: String? = null,
    private val placementName: String? = null
) : TJPlacementListener, LifecycleObserver, AdvertisingHelper {

    init {
        activity.lifecycle.addObserver(this)
    }

    private var mOfferwall: TJPlacement? = null
    private var mShowOfferwall = false
    private var mListener: AdvertisingListener? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        Tapjoy.onActivityStart(activity)
        if (!Tapjoy.isConnected()) {
            connect()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        Tapjoy.onActivityStop(activity)
    }

    private val connectListener = object : TJConnectListener {
        override fun onConnectSuccess() {
            mOfferwall = if (placementName == null) {
                Tapjoy.getPlacement("Offerwall", this@TapjoyImpl)
            } else {
                Tapjoy.getPlacement(placementName, this@TapjoyImpl)
            }
            mOfferwall?.requestContent()
        }

        override fun onConnectFailure() {
            mListener?.onAdvertisingLoadFailed()
        }
    }

    private fun connect() {
        val connectFlags = Hashtable<String, Any>()
        if (BuildConfig.DEBUG) {
            connectFlags[TapjoyConnectFlag.ENABLE_LOGGING] = "true"
            Tapjoy.setDebugEnabled(true)
        }
        if (userId != null) {
            connectFlags[TapjoyConnectFlag.USER_ID] = userId
        }
        val sdkKey = activity.getString(resId)
        Tapjoy.connect(
            activity.applicationContext,
            sdkKey,
            connectFlags,
            connectListener
        )
    }

    override fun onRequestSuccess(tjPlacement: TJPlacement?) {

    }

    override fun onRequestFailure(tjPlacement: TJPlacement?, tjError: TJError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onContentReady(tjPlacement: TJPlacement?) {
        if (mShowOfferwall) {
            tjPlacement?.showContent()
        }
    }

    override fun onContentShow(tjPlacement: TJPlacement?) {
        mListener?.onAdvertisingShow()
        mShowOfferwall = false
        tjPlacement?.requestContent()
    }

    override fun onContentDismiss(tjPlacement: TJPlacement?) {

    }

    override fun onPurchaseRequest(
        tjPlacement: TJPlacement?,
        tjActionRequest: TJActionRequest?,
        p2: String?
    ) {
    }

    override fun onRewardRequest(
        tjPlacement: TJPlacement?,
        tjActionRequest: TJActionRequest?,
        p2: String?,
        p3: Int
    ) {
    }

    override fun onClick(tjPlacement: TJPlacement?) {
    }

    override fun showOfferwall(placementName: String?, listener: AdvertisingListener?) {
        mShowOfferwall = true
        mListener = listener
        if (mOfferwall != null && mOfferwall!!.isContentReady) {
            mOfferwall!!.showContent()
            mShowOfferwall = false
        }
    }

    override fun showBanner(
        bannerContainer: FrameLayout,
        placementName: String?,
        size: ISBannerSize?,
        listener: AdvertisingListener?
    ) {
    }

    override fun showInterstitial(placementName: String?, listener: AdvertisingListener?) {
    }

    override fun showRewardedVideo(placementName: String?, listener: AdvertisingListener?) {
    }
}