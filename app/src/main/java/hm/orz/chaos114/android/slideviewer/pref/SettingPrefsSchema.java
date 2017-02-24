package hm.orz.chaos114.android.slideviewer.pref;

import com.rejasupotaro.android.kvs.annotations.Key;
import com.rejasupotaro.android.kvs.annotations.Table;

/**
 * Manage SharedPreferences data for SettingActivity.
 */
@Table(name = "setting")
public interface SettingPrefsSchema {
    @Key(name = "enable_ocr")
    boolean enableOcr = false;
}
