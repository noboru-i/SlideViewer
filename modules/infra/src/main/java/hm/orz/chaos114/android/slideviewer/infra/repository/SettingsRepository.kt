package hm.orz.chaos114.android.slideviewer.infra.repository

import android.content.Context

import hm.orz.chaos114.android.slideviewer.infra.pref.SettingPrefs

class SettingsRepository(context: Context) {
    private val settingPrefs: SettingPrefs

    var enableOcr: Boolean
        get() = settingPrefs.enableOcr
        set(enableOcr) {
            settingPrefs.enableOcr = enableOcr
        }

    var selectedLanguage: String
        get() = settingPrefs.selectedLanguage
        set(selectedLanguage) {
            settingPrefs.selectedLanguage = selectedLanguage
        }

    init {
        settingPrefs = SettingPrefs.get(context)
    }
}
