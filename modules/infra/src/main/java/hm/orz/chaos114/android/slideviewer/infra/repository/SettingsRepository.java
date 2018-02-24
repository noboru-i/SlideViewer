package hm.orz.chaos114.android.slideviewer.infra.repository;

import android.content.Context;

import hm.orz.chaos114.android.slideviewer.infra.pref.SettingPrefs;

public class SettingsRepository {
    private SettingPrefs settingPrefs;

    public SettingsRepository(Context context) {
        settingPrefs = SettingPrefs.get(context);
    }

    public void setEnableOcr(boolean enableOcr) {
        settingPrefs.setEnableOcr(enableOcr);
    }

    public boolean getEnableOcr() {
        return settingPrefs.getEnableOcr();
    }

    public void setSelectedLanguage(String selectedLanguage) {
        settingPrefs.setSelectedLanguage(selectedLanguage);
    }

    public String getSelectedLanguage() {
        return settingPrefs.getSelectedLanguage();
    }
}
