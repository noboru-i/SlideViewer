package hm.orz.chaos114.android.slideviewer.infra.pref

import com.rejasupotaro.android.kvs.annotations.Key
import com.rejasupotaro.android.kvs.annotations.Table

/**
 * Manage SharedPreferences data for SettingActivity.
 */
@Table(name = "setting")
interface SettingPrefsSchema {
    companion object {
        @Key(name = "enable_ocr")
        val enableOcr = false

        @Key(name = "selected_language")
        val selectedLanguage: String? = null
    }
}
