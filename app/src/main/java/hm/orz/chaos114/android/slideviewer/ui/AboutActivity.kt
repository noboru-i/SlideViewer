package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import hm.orz.chaos114.android.slideviewer.BuildConfig
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivityAboutBinding
import hm.orz.chaos114.android.slideviewer.util.IntentUtil

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)

        initToolbar()
        initActions()
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

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            it.setHomeButtonEnabled(true)
        }
    }

    private fun initActions() {
        binding.aboutVersion.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)
        binding.aboutGitHub.setOnClickListener { _ -> IntentUtil.browse(this, "https://github.com/noboru-i/SlideViewer") }
        binding.aboutOtherApp.setOnClickListener { _ -> IntentUtil.browse(this, "https://play.google.com/store/apps/developer?id=noboru") }
        binding.aboutLicense.setOnClickListener { _ -> startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }
    }

    companion object {

        fun start(context: Context) = context.startActivity(Intent(context, AboutActivity::class.java))
    }
}
