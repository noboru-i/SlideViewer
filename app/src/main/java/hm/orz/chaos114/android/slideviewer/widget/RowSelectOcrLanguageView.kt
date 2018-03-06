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
class RowSelectOcrLanguageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: RowSelectOcrLanguageBinding
    private var language: Language? = null
    private var loading: Boolean = false
    private var listener: RowSelectOcrLanguageViewListener? = null

    init {

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.row_select_ocr_language, this, true)
    }

    fun setData(lang: Language?) {
        this.language = lang

        binding.label.text = language!!.label
        if (loading) {
            binding.status.setText(R.string.select_ocr_language_downloading)
        } else {
            binding.status.setText(if (DirectorySettings.hasFile(context, language!!))
                R.string.select_ocr_language_downloaded
            else
                R.string.select_ocr_language_not_downloaded)
        }
        val settingsRepository = SettingsRepository(context)
        binding.languageSwitch.isChecked = language!!.id == settingsRepository.selectedLanguage

        binding.languageSwitch.setOnClickListener { v ->
            if (listener != null) {
                listener!!.onChangeState(this, language!!, (v as SwitchCompat).isChecked)
            }
        }
        binding.languageSwitch.visibility = if (loading) View.GONE else View.VISIBLE
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    fun showLoading(loading: Boolean) {
        this.loading = loading

        // update view
        setData(language)
    }

    fun setListener(listener: RowSelectOcrLanguageViewListener) {
        this.listener = listener
    }

    interface RowSelectOcrLanguageViewListener {
        fun onChangeState(view: RowSelectOcrLanguageView, language: Language, isChecked: Boolean)
    }
}
