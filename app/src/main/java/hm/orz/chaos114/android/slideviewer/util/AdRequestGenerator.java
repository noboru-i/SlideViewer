package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;

import hm.orz.chaos114.android.slideviewer.R;

public final class AdRequestGenerator {
    private AdRequestGenerator() {
        // no-op
    }

    public static AdRequest generate(Context context) {
        String testDeviceId = context.getString(R.string.admob_test_device);
        return new AdRequest.Builder().addTestDevice(testDeviceId).build();
    }
}
