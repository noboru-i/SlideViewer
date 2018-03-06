package hm.orz.chaos114.android.slideviewer.ui

import android.text.TextUtils
import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        if (priority != Log.ERROR) {
            FirebaseCrash.log(message)
            return
        }
        var throwable = t
        if (throwable == null && !TextUtils.isEmpty(message)) {
            throwable = Exception(message)
        }
        FirebaseCrash.report(throwable)
    }
}
