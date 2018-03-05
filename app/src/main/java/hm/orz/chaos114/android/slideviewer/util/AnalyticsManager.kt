package hm.orz.chaos114.android.slideviewer.util

import android.content.Context
import android.net.Uri
import android.os.Bundle

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.firebase.analytics.FirebaseAnalytics

import java.util.Locale

import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import timber.log.Timber

object AnalyticsManager {

    private var sAppContext: Context? = null
    private var mTracker: Tracker? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun sendScreenView(screenName: String) {
        if (!canSend()) {
            Timber.d("Screen View NOT recorded (analytics disabled or not ready).")
            return
        }

        mTracker!!.setScreenName(screenName)
        mTracker!!.send(HitBuilders.AppViewBuilder().build())
        Timber.d("Screen View recorded: %s", screenName)
    }

    fun sendChangePageEvent(category: String, url: String, page: Int) {
        sendEvent(category, "CHANGE_PAGE", Integer.toString(page))

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url))
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "slide")
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "slide")
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, page.toDouble())
        sendFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
    }

    fun sendStartEvent(category: String, url: String) {
        sendEvent(category, "START", url)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "slide")
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url))
        sendFirebaseEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun updateUserProperty() {
        if (!canSend()) {
            Timber.d("Analytics update user property ignored (analytics disabled or not ready).")
            return
        }

        val talkRepository = TalkRepository(sAppContext!!)
        val count = talkRepository.count()
        mFirebaseAnalytics!!.setUserProperty("slide_count", String.format(Locale.getDefault(), "%d", count))
    }


    @Synchronized
    fun initializeAnalyticsTracker(context: Context) {
        sAppContext = context
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.global_tracker)
        }
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    private fun canSend(): Boolean {
        return sAppContext != null && mTracker != null
    }

    private fun sendEvent(category: String, action: String, label: String) {
        if (!canSend()) {
            Timber.d("Analytics event ignored (analytics disabled or not ready).")
            return
        }

        mTracker!!.send(HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(0)
                .build())
    }

    private fun sendFirebaseEvent(event: String, bundle: Bundle) {
        mFirebaseAnalytics!!.logEvent(event, bundle)
    }

    private fun getPath(url: String): String {
        val uri = Uri.parse(url)
        return uri.path
    }
}// no-op
