package hm.orz.chaos114.android.slideviewer.util

import android.content.Context

import com.google.android.gms.ads.AdRequest

import hm.orz.chaos114.android.slideviewer.R
import javax.inject.Inject

class AdRequestGenerator @Inject constructor(
        private val context: Context
) {

    fun generate(): AdRequest {
        val testDeviceId = context.getString(R.string.admob_test_device)
        return AdRequest.Builder().addTestDevice(testDeviceId).build()
    }
}
