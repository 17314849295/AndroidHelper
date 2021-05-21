package com.example.android.helper.library.ads

import android.widget.FrameLayout
import com.ironsource.mediationsdk.ISBannerSize

interface AdvertisingHelper {

    fun showOfferwall(placementName: String? = null, listener: AdvertisingListener? = null)

    fun showBanner(
        bannerContainer: FrameLayout,
        placementName: String? = null,
        size: ISBannerSize? = null,
        listener: AdvertisingListener? = null
    )

    fun showInterstitial(placementName: String? = null, listener: AdvertisingListener? = null)

    fun showRewardedVideo(placementName: String? = null, listener: AdvertisingListener? = null)
}