package hm.orz.chaos114.android.slideviewer.ui;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import timber.log.Timber;

public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }
        if (priority != Log.ERROR) {
            FirebaseCrash.log(message);
            return;
        }
        Throwable throwable = t;
        if (throwable == null && !TextUtils.isEmpty(message)) {
            throwable = new Exception(message);
        }
        FirebaseCrash.report(throwable);
    }
}
