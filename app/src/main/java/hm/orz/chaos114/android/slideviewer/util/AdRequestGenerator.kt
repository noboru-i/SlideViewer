package hm.orz.chaos114.android.slideviewer.util

import android.content.Context

import com.google.android.gms.ads.AdRequest

import hm.orz.chaos114.android.slideviewer.R

object AdRequestGenerator {

    fun generate(context: Context): AdRequest {
        val testDeviceId = context.getString(R.string.admob_test_device)
        return AdRequest.Builder().addTestDevice(testDeviceId).build()
    }
}// no-op
