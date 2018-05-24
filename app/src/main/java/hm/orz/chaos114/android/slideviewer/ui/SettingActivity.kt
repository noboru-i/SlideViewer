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
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import dagger.android.AndroidInjection
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySettingBinding
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager
import javax.inject.Inject

private const val selectOcrLanguageActivityClassname = "hm.orz.chaos114.android.slideviewer.ocr.ui.SelectOcrLanguageActivity"

class SettingActivity : AppCompatActivity() {

    private val listener = SplitInstallStateUpdatedListener { state ->
        state.moduleNames().forEach { name ->
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    displayLoadingState("Downloading $name")
                }

                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    startIntentSender(state.resolutionIntent().intentSender, null, 0, 0, 0)
                }

                SplitInstallSessionStatus.INSTALLED -> {
                    onSuccessfulLoad()
                }

                SplitInstallSessionStatus.INSTALLING -> displayLoadingState("Installing $name")
            }
        }
    }


    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private lateinit var binding: ActivitySettingBinding
    private lateinit var splitInstallManager: SplitInstallManager
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        splitInstallManager = SplitInstallManagerFactory.create(this)

        analyticsManager.sendScreenView(TAG)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        init()
    }

    override fun onResume() {
        splitInstallManager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        splitInstallManager.unregisterListener(listener)
        super.onPause()
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
        binding.selectLanguageLayout.setOnClickListener listener@{ _ ->
            val name = "ocr"
            if (splitInstallManager.installedModules.contains(name)) {
                onSuccessfulLoad()
                return@listener
            }

            val request = SplitInstallRequest.newBuilder()
                    .addModule(name)
                    .build()
            splitInstallManager.startInstall(request)
            displayLoadingState("Starting install for $name")
        }
    }

    private fun displayLoadingState(message: String) {
        loadingDialog = LoadingDialogFragment.newInstance()
        loadingDialog!!.show(fragmentManager, null)
    }

    private fun onSuccessfulLoad() {
        loadingDialog?.dismiss()

        Intent().setClassName(packageName, selectOcrLanguageActivityClassname)
                .also {
                    startActivity(it)
                }
    }

    companion object {
        private val TAG = SettingActivity::class.java.getSimpleName()

        fun start(context: Context) =
                context.startActivity(Intent(context, SettingActivity::class.java))
    }
}
