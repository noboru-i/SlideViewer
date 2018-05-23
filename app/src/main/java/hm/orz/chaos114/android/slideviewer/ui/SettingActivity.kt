package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import dagger.android.AndroidInjection

import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySettingBinding
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager
import javax.inject.Inject

class SettingActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)

        analyticsManager.sendScreenView(TAG)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // TODO move to ViewModel
    private fun init() {
        binding.settingSwitch.isChecked = settingsRepository.enableOcr

        binding.settingSwitch.setOnClickListener listener@{ v ->
            val view = v as SwitchCompat
            val isChecked = view.isChecked
            if (isChecked && TextUtils.isEmpty(settingsRepository.selectedLanguage)) {
                view.isChecked = false
                Toast.makeText(this, R.string.setting_error_before_download, Toast.LENGTH_LONG).show()
                return@listener
            }
            settingsRepository.enableOcr = isChecked
        }
//        binding.selectLanguageLayout.setOnClickListener { _ -> SelectOcrLanguageActivity.start(this) }
    }

    companion object {
        private val TAG = SettingActivity::class.java.getSimpleName()

        fun start(context: Context) =
                context.startActivity(Intent(context, SettingActivity::class.java))
    }
}
