package hm.orz.chaos114.android.slideviewer.util

import android.net.Uri

import timber.log.Timber

object UrlHelper {

    fun isSpeakerDeckUrl(uri: Uri): Boolean {
        if ("https" != uri.scheme) {
            Timber.d("unexpected scheme")
            return false
        }
        if ("speakerdeck.com" != uri.host) {
            Timber.d("unexpected host")
            return false
        }
        return true
    }

    fun canOpen(uri: Uri): Boolean {
        if (uri.pathSegments.size < 2) {
            Timber.d("unexpected path segments")
            return false
        }
        return true
    }
}// no-op
