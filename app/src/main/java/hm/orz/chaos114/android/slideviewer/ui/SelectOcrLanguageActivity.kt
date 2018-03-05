package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySelectOcrLanguageBinding
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.ocr.LanguageDownloader
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings
import hm.orz.chaos114.android.slideviewer.widget.RowSelectOcrLanguageView
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*

class SelectOcrLanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectOcrLanguageBinding
    private var adapter: BaseAdapter? = null

    private val listener = object : RowSelectOcrLanguageView.RowSelectOcrLanguageViewListener {
        override fun onChangeState(view: RowSelectOcrLanguageView, language: Language, isChecked: Boolean) {
            Timber.d("onChangeState: %b", isChecked)
            if (!isChecked) {
                updatePrefs(null)
                return
            }

            if (DirectorySettings.hasFile(this@SelectOcrLanguageActivity, language)) {
                updatePrefs(language)
                return
            }

            view.showLoading(true)
            val downloader = LanguageDownloader()
            downloader.download(this@SelectOcrLanguageActivity, language)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ ->
                        Toast.makeText(this@SelectOcrLanguageActivity, "download succeeded.", Toast.LENGTH_SHORT).show()
                        adapter!!.notifyDataSetChanged()
                        updatePrefs(language)
                        view.showLoading(false)
                    }
        }

        private fun updatePrefs(language: Language?) {
            val settingsRepository = SettingsRepository(this@SelectOcrLanguageActivity)
            settingsRepository.selectedLanguage = language?.id!!
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_ocr_language)

        setSupportActionBar(binding.toolbar)
        if (supportActionBar == null) {
            throw AssertionError("getSupportActionBar() needs non-null.")
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        adapter = Adapter()
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
            val view: RowSelectOcrLanguageView
            if (convertView == null) {
                view = RowSelectOcrLanguageView(this@SelectOcrLanguageActivity)
            } else {
                view = convertView as RowSelectOcrLanguageView
            }

            val rowData = getItem(position)
            view.setData(rowData)
            view.setListener(listener)

            return view
        }
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, SelectOcrLanguageActivity::class.java)
            context.startActivity(intent)
        }
    }
}
