package hm.orz.chaos114.android.slideviewer.ocr.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import dagger.android.AndroidInjection
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.ocr.LanguageDownloader
import hm.orz.chaos114.android.slideviewer.ocr.R
import hm.orz.chaos114.android.slideviewer.ocr.databinding.ActivitySelectOcrLanguageBinding
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings
import hm.orz.chaos114.android.slideviewer.ocr.widget.RowSelectOcrLanguageView
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SelectOcrLanguageActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private lateinit var binding: ActivitySelectOcrLanguageBinding
    private lateinit var adapter: BaseAdapter

    private val listener = object : RowSelectOcrLanguageView.RowSelectOcrLanguageViewListener {
        override fun onChangeState(view: RowSelectOcrLanguageView, language: Language, isChecked: Boolean) {
            Timber.d("onChangeState: %b", isChecked)
            if (!isChecked) {
                updatePrefs(null)
                return
            }

            val context = this@SelectOcrLanguageActivity
            if (DirectorySettings.hasFile(context, language)) {
                updatePrefs(language)
                return
            }

            view.showLoading(true)
            val downloader = LanguageDownloader()
            downloader.download(context, language)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ ->
                        Toast.makeText(context, "download succeeded.", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                        updatePrefs(language)
                        view.showLoading(false)
                    }
        }

        private fun updatePrefs(language: Language?) {
            settingsRepository.selectedLanguage = language?.id!!
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_ocr_language)
        adapter = Adapter()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        binding.languageList.adapter = adapter
    }

    private inner class Adapter : BaseAdapter() {

        private val rowDataList: MutableList<Language>

        init {
            rowDataList = ArrayList()
            rowDataList.add(Language("eng", "English", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/eng.traineddata?raw=true"))
            rowDataList.add(Language("jpn", "Japanese", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/jpn.traineddata?raw=true"))
        }

        override fun getCount(): Int {
            return rowDataList.size
        }

        override fun getItem(position: Int): Language {
            return rowDataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = if (convertView == null) {
                RowSelectOcrLanguageView(this@SelectOcrLanguageActivity)
            } else {
                convertView as RowSelectOcrLanguageView
            }

            view.setData(getItem(position))
            view.setListener(listener)

            return view
        }
    }

    companion object {

        fun start(context: Context) =
                context.startActivity(Intent(context, SelectOcrLanguageActivity::class.java))
    }
}
