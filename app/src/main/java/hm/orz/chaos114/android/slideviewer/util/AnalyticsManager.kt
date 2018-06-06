package hm.orz.chaos114.android.slideviewer.util

import android.app.Application
import android.net.Uri
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import java.util.Locale
import javax.inject.Inject

class AnalyticsManager @Inject constructor(
        private val app: Application
) {

    private val mFirebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    fun sendChangePageEvent(url: String, page: Int) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url))
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "slide")
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "slide")
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, page.toDouble())
        sendFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
    }

    fun sendStartEvent(url: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "slide")
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url))
        sendFirebaseEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun updateUserProperty() {
        val talkRepository = TalkRepository(app)
        val count = talkRepository.count()
        mFirebaseAnalytics.setUserProperty("slide_count", String.format(Locale.getDefault(), "%d", count))
    }

    private fun sendFirebaseEvent(event: String, bundle: Bundle) {
        mFirebaseAnalytics.logEvent(event, bundle)
    }

    private fun getPath(url: String): String {
        val uri = Uri.parse(url)
        return uri.path
    }
}
