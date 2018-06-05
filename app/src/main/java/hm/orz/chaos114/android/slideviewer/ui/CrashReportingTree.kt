package hm.orz.chaos114.android.slideviewer.ui

import android.text.TextUtils
import android.util.Log
import com.crashlytics.android.Crashlytics

import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        if (priority != Log.ERROR) {
            Crashlytics.log(message)
            return
        }

        val throwable = if (t == null && !TextUtils.isEmpty(message)) {
            Exception(message)
        } else {
            t
        }
        Crashlytics.logException(throwable)
    }
}
