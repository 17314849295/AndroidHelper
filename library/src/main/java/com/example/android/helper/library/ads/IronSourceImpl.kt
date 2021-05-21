package com.example.android.helper.library.ads

import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.android.helper.library.BuildConfig
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.BannerListener
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.ironsource.mediationsdk.sdk.OfferwallListener
import com.ironsource.mediationsdk.sdk.RewardedVideoListener

/**
 * 子类继承之后，需要处理activity生命周期，防止内存泄露
 */
open class IronSourceImpl(
    private val activity: FragmentActivity,
    @StringRes private val resId: Int,
    adUnits: Array<out IronSource.AD_UNIT>,
    userId: String? = null
) : LifecycleObserver, RewardedVideoListener, InterstitialListener, BannerListener,
    OfferwallListener, AdvertisingHelper {

    init {
        activity.lifecycle.addObserver(this)
    }

    private var mShowInterstitial = false
    private var mBanner: IronSourceBannerLayout? = null
    private var mListener: AdvertisingListener? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        IronSource.onResume(activity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        IronSource.onPause(activity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestory() {
        if (mBanner != null) {
            IronSource.destroyBanner(mBanner)
        }
    }

    init {
        val appKey = activity.getString(resId)
        if (userId != null) {
            IronSource.setUserId(userId)
        }
        IronSource.init(activity, appKey, *adUnits)
        if (BuildConfig.DEBUG) {
            IntegrationHelper.validateIntegration(activity)
            IronSource.setAdaptersDebug(true)
        }
        if (adUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
            IronSource.loadInterstitial()
            IronSource.setInterstitialListener(this)
        }
        if (adUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
            IronSource.setRewardedVideoListener(this)
        }
        if (adUnits.contains(IronSource.AD_UNIT.OFFERWALL)) {
            IronSource.setOfferwallListener(this)
        }
    }

    override fun showRewardedVideo(placementName: String?, listener: AdvertisingListener?) {
        mListener = listener
        if (placementName == null) {
            IronSource.showRewardedVideo()
        } else {
            IronSource.showRewardedVideo(placementName)
        }
    }

    override fun showInterstitial(placementName: String?, listener: AdvertisingListener?) {
        mShowInterstitial = true
        mListener = listener
        if (!IronSource.isInterstitialReady()) {
            IronSource.loadInterstitial()
        }
        if (!IronSource.isInterstitialReady()) return
        if (placementName == null) {
            IronSource.showInterstitial()
        } else {
            IronSource.showInterstitial(placementName)
        }
        mShowInterstitial = false
    }

    override fun showBanner(
        bannerContainer: FrameLayout,
        placementName: String?,
        size: ISBannerSize?,
        listener: AdvertisingListener?
    ) {
        mListener = listener
        mBanner = if (size != null) {
            IronSource.createBanner(activity, size)
        } else {
            IronSource.createBanner(activity, ISBannerSize.SMART)
        }
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        bannerContainer.addView(mBanner, 0, layoutParams)
        mBanner?.bannerListener = this
        if (placementName != null) {
            IronSource.loadBanner(mBanner, placementName)
        } else {
            IronSource.loadBanner(mBanner)
        }
    }

    override fun showOfferwall(placementName: String?, listener: AdvertisingListener?) {
        mListener = listener
        if (placementName != null) {
            IronSource.showOfferwall(placementName)
        } else {
            IronSource.showOfferwall()
        }
    }

    override fun onOfferwallAvailable(available: Boolean) {
        if (!available) {
            mListener?.onAdvertisingLoadFailed()
        }
    }

    override fun onOfferwallOpened() {
        mListener?.onAdvertisingShow()
    }

    override fun onOfferwallShowFailed(ironSourceError: IronSourceError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onOfferwallAdCredited(p0: Int, p1: Int, p2: Boolean): Boolean {
        return false
    }

    override fun onGetOfferwallCreditsFailed(ironSourceError: IronSourceError?) {
    }

    override fun onOfferwallClosed() {
    }

    override fun onBannerAdLoaded() {
        mBanner?.visibility = View.VISIBLE
    }

    override fun onBannerAdLoadFailed(ironSourceError: IronSourceError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onBannerAdClicked() {
    }

    override fun onBannerAdScreenPresented() {
        mListener?.onAdvertisingShow()
    }

    override fun onBannerAdScreenDismissed() {
    }

    override fun onBannerAdLeftApplication() {
    }

    override fun onInterstitialAdReady() {
        if (mShowInterstitial) {
            IronSource.showInterstitial()
        }
    }

    override fun onInterstitialAdLoadFailed(error: IronSourceError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onInterstitialAdOpened() {
    }

    override fun onInterstitialAdClosed() {
        IronSource.loadInterstitial()
    }

    override fun onInterstitialAdShowSucceeded() {
        mListener?.onAdvertisingShow()
        mShowInterstitial = false
    }

    override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onInterstitialAdClicked() {
    }

    override fun onRewardedVideoAdOpened() {
        mListener?.onAdvertisingShow()
    }

    override fun onRewardedVideoAdClosed() {
    }

    override fun onRewardedVideoAvailabilityChanged(available: Boolean) {
        if (!available) {
            mListener?.onAdvertisingLoadFailed()
        }
    }

    override fun onRewardedVideoAdStarted() {
    }

    override fun onRewardedVideoAdEnded() {
    }

    override fun onRewardedVideoAdRewarded(placement: Placement?) {
    }

    override fun onRewardedVideoAdShowFailed(error: IronSourceError?) {
        mListener?.onAdvertisingLoadFailed()
    }

    override fun onRewardedVideoAdClicked(placement: Placement?) {
    }
}