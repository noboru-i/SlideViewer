package hm.orz.chaos114.android.slideviewer.widget

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.RowSelectOcrLanguageBinding
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings

/**
 * List row of select ocr language.
 */
class RowSelectOcrLanguageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: RowSelectOcrLanguageBinding
            = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.row_select_ocr_language, this, true)

    private var language: Language? = null
    private var loading: Boolean = false
    private var listener: RowSelectOcrLanguageViewListener? = null

    fun setData(lang: Language) {
        this.language = lang

        binding.label.text = lang.label
        binding.status.setText(when {
            loading ->
                R.string.select_ocr_language_downloading
            DirectorySettings.hasFile(context, lang) ->
                R.string.select_ocr_language_downloaded
            else ->
                R.string.select_ocr_language_not_downloaded
        })
        // TODO inject by Dagger?
        val settingsRepository = SettingsRepository(context)
        binding.languageSwitch.isChecked = lang.id == settingsRepository.selectedLanguage

        binding.languageSwitch.setOnClickListener { v ->
            listener?.onChangeState(this, lang, (v as SwitchCompat).isChecked)
        }
        binding.languageSwitch.visibility = if (loading) View.GONE else View.VISIBLE
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    fun showLoading(loading: Boolean) {
        this.loading = loading

        // update view
        setData(language!!)
    }

    fun setListener(listener: RowSelectOcrLanguageViewListener) {
        this.listener = listener
    }

    interface RowSelectOcrLanguageViewListener {
        fun onChangeState(view: RowSelectOcrLanguageView, language: Language, isChecked: Boolean)
    }
}
