package hm.orz.chaos114.android.slideviewer.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.RowSelectOcrLanguageBinding;
import hm.orz.chaos114.android.slideviewer.pref.SettingPrefs;
import hm.orz.chaos114.android.slideviewer.util.OcrUtil;
import lombok.Setter;

/**
 * List row of select ocr language.
 */
public class RowSelectOcrLanguageView extends LinearLayout {

    private RowSelectOcrLanguageBinding binding;
    @Setter
    private RowSelectOcrLanguageViewListener listener;

    public RowSelectOcrLanguageView(Context context) {
        this(context, null);
    }

    public RowSelectOcrLanguageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowSelectOcrLanguageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.row_select_ocr_language, this, true);
    }

    public void setData(OcrUtil.Language language) {
        binding.label.setText(language.getLabel());
        binding.status.setText(OcrUtil.hasFile(getContext(), language)
                ? R.string.select_ocr_language_downloaded
                : R.string.select_ocr_language_not_downloaded);
        SettingPrefs settingPrefs = SettingPrefs.get(getContext());
        binding.languageSwitch.setChecked(language.getId().equals(settingPrefs.getSelectedLanguage()));

        binding.languageSwitch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeState(language, ((SwitchCompat) v).isChecked());
            }
        });
    }

    public interface RowSelectOcrLanguageViewListener {
        void onChangeState(OcrUtil.Language language, boolean isChecked);
    }
}
